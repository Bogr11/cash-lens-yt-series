package com.bornik.cashlens.inbound;

import com.bornik.cashlens.processor.ProcessingFacade;
import com.fasterxml.jackson.annotation.OptBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
class InboundService {

    private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private final InboundMessageRepository repository;
    private final ProcessingFacade processingFacade;

    AcceptResult receiveAsText(String externalId, String payload) {
        return acceptMsg(externalId, () -> InboundMessage.receivedAsText(externalId, payload));
    }

    AcceptResult receiveAsFile(String externalId, byte[] content, String contentType, InputSource source) {
        return acceptMsg(externalId, () -> InboundMessage.receivedAsFile(externalId, content, contentType, source));
    }

    private AcceptResult acceptMsg(String externalId, Supplier<InboundMessage> message) {
        if (repository.existsByExternalId(externalId)) {
            log.info("Received duplicate request. Skipping. ExternalId={}", externalId);
            return AcceptResult.DUPLICATE;
        }

        InboundMessage saved = repository.save(message.get());

        CompletableFuture
                .runAsync(() -> process(saved), EXECUTOR)
                .whenComplete((r, t) -> {
                    if (t == null) {
                        log.info("Processed: ExternalId = {}. Message = {}.", externalId, saved);
                    } else {
                        log.error("Processing failed. ExternalId = {}", externalId, t);
                    }
                });

        return AcceptResult.ACCEPTED;
    }

    private void process(InboundMessage message) {
        try {
            processingFacade.process(new InboundMessageDto(message));
            message.markProcessed();
            repository.save(message);
            log.info("InboundMessage processing succeeded {}", message);
        } catch (Exception e) {
            message.markFailed(e.getMessage());
            repository.save(message);
            log.error("InboundMessage processing failed {}", message, e);
        }
    }

    Optional<InboundMessage> findByExternalId(String externalId) {
        return repository.findByExternalId(externalId);
    }
}

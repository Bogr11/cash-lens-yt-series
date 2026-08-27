package com.bornik.cashlens.inbound;

import com.bornik.cashlens.processor.ProcessingFacade;
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

    AcceptResult receiveAsText(String accountId, String externalId, String payload) {
        return acceptMsg(accountId, externalId, () -> InboundMessage.receivedAsText(accountId, externalId, payload));
    }

    AcceptResult receiveAsFile(String accountId, String externalId, byte[] content, String contentType, InputSource source) {
        return acceptMsg(accountId, externalId, () -> InboundMessage.receivedAsFile(accountId, externalId, content, contentType, source));
    }

    private AcceptResult acceptMsg(String accountId, String externalId, Supplier<InboundMessage> message) {
        if (repository.existsByAccountIdAndExternalId(accountId, externalId)) {
            log.info("Received duplicate request. Skipping. AccountId={}, ExternalId={}", accountId, externalId);
            return AcceptResult.DUPLICATE;
        }

        InboundMessage saved = repository.save(message.get());

        CompletableFuture
                .runAsync(() -> process(saved), EXECUTOR)
                .whenComplete((r, t) -> {
                    if (t == null) {
                        log.info("Processed: AccountId={}, ExternalId = {}. Message = {}.", accountId, externalId, saved);
                    } else {
                        log.error("Processing failed. AccountId={}, ExternalId = {}", accountId, externalId, t);
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

    Optional<InboundMessage> findByAccountIdAndExternalId(String accountId, String externalId) {
        return repository.findByAccountIdAndExternalId(accountId, externalId);
    }
}

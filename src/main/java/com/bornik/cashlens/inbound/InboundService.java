package com.bornik.cashlens.inbound;

import com.bornik.cashlens.processor.ProcessingFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    void receiveAsText(String accountId, String externalId, String payload) {
        acceptMsg(accountId, externalId, () -> InboundMessage.receivedAsText(accountId, externalId, payload));
    }

    void receiveAsFile(String accountId, String externalId, byte[] content, String contentType, InputSource source) {
        acceptMsg(accountId, externalId, () -> InboundMessage.receivedAsFile(accountId, externalId, content, contentType, source));
    }

    private void acceptMsg(String accountId, String externalId, Supplier<InboundMessage> message) {
        if (repository.existsByAccountIdExternalId(accountId, externalId)) {
            log.info("Received duplicate request. Skipping. ExternalId={}", externalId);
            return;
        }

        InboundMessage saved = repository.save(message.get());

        CompletableFuture
                .runAsync(() -> process(saved), EXECUTOR)
                .whenComplete((r, t) -> log.info("Processed: ExternalId = {}. Message = {}.", externalId, message.get()));
    }

    private void process(InboundMessage message) {
        try {
            processingFacade.process(new InboundMessageDto(message));
            message.setStatus(ProcessingStatus.PROCESSED);
            repository.save(message);
            log.info("InboundMessage processing succeeded {}", message);
        } catch (Exception e) {
            message.setStatus(ProcessingStatus.FAILED);
            repository.save(message);
            log.error("InboundMessage processing failed {}", message, e);
        }
    }

}

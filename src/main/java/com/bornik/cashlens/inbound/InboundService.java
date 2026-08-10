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

    void receive(String externalId, String payload) {
        accept(externalId, () -> InboundMessage.received(externalId, payload, InputSource.TEXT_MESSAGE));
    }

    void receiveFile(String externalId, byte[] content, String fileName, InputSource source) {
        accept(externalId, () -> InboundMessage.receivedFile(externalId, fileName, content, source));
    }

    /**
     * The message is saved BEFORE the handoff, so 202 means it is durable —
     * not that it sits in an in-memory queue.
     */
    private void accept(String externalId, Supplier<InboundMessage> message) {
        if (repository.existsByExternalId(externalId)) {
            log.info("Received duplicate request. Skipping. ExternalId={}", externalId);
            return;
        }

        InboundMessage saved = repository.save(message.get());
        CompletableFuture.runAsync(() -> process(saved), EXECUTOR);
    }

    private void process(InboundMessage message) {
        try {
            processingFacade.process(InboundMessageDto.of(message));
            // Drops the stored photo along the way — the parsed expense is what we keep.
            message.markProcessed();
            repository.save(message);
            log.info("InboundMessage processing succeeded {}", message);
        } catch (Exception e) {
            message.markFailed();
            repository.save(message);
            log.error("InboundMessage processing failed {}", message, e);
        }
    }

}

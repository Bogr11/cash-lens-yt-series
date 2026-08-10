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
        accept(externalId, () -> InboundMessage.receivedText(externalId, payload));
    }

    void receiveFile(String externalId, byte[] content, String contentType, InputSource source) {
        accept(externalId, () -> InboundMessage.receivedFile(externalId, content, contentType, source));
    }

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

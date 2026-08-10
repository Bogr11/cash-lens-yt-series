package com.bornik.cashlens.inbound;

import com.bornik.cashlens.processor.ProcessingFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
class InboundService {

    private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private final InboundMessageRepository repository;
    private final ReceiptStorage receiptStorage;
    private final ProcessingFacade processingFacade;

    void receive(String externalId, String payload) {
        if (isDuplicate(externalId)) {
            return;
        }
        accept(externalId, payload, InputSource.TEXT_MESSAGE);
    }

    void receivePhoto(String externalId, byte[] bytes, String originalFilename) {
        if (isDuplicate(externalId)) {
            return;
        }
        // Store only after the duplicate check — a repeat should not cost a file write.
        accept(externalId, receiptStorage.store(bytes, originalFilename), InputSource.PHOTO);
    }

    private boolean isDuplicate(String externalId) {
        boolean duplicate = repository.existsByExternalId(externalId);
        if (duplicate) {
            log.info("Received duplicate request. Skipping. ExternalId={}", externalId);
        }
        return duplicate;
    }

    /**
     * The message is saved BEFORE the handoff, so 202 means it is durable —
     * not that it sits in an in-memory queue.
     */
    private void accept(String externalId, String payload, InputSource source) {
        InboundMessage message = repository.save(InboundMessage.received(externalId, payload, source));
        CompletableFuture.runAsync(() -> process(message), EXECUTOR);
    }

    private void process(InboundMessage message) {
        try {
            processingFacade.process(InboundMessageDto.of(message));
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

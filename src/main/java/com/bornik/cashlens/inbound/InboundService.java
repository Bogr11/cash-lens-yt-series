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
    private final ProcessingFacade processingFacade;

    void receive(String externalId, String payload) {
        if (repository.existsByExternalId(externalId)) {
            log.info("Received duplicate request. Skipping. ExternalId={}", externalId);
            return;
        }

        InboundMessage message = repository.save(InboundMessage.received(externalId, payload, InputSource.TEXT_MESSAGE));

        CompletableFuture
                .runAsync(() -> process(message), EXECUTOR)
                .whenComplete((r, t) -> log.info("Processed: ExternalId = {}. Payload = {}.", externalId, payload));
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

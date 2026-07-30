package com.bornik.cashlens.inbound;

import com.bornik.cashlens.processor.ProcessingFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class InboundService {

    private final InboundMessageRepository repository;
    private final ProcessingFacade processingFacade;

    void receive(String payload) {
        InboundMessage message = repository.save(new InboundMessage(payload, InputSource.TEXT_MESSAGE));
        processingFacade.process(new InboundMessageDto(message));
    }

}

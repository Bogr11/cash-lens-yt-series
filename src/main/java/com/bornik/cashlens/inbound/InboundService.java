package com.bornik.cashlens.inbound;

import com.bornik.cashlens.processor.ParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class InboundService {

    private final InboundMessageRepository repository;
    private final ParserService parserService;

    void process(String payload) {
        InboundMessage message = repository.save(new InboundMessage(payload, InputSource.TEXT_MESSAGE));
        parserService.parseAndSave(new InboundMessageDto(message));
    }

}

package com.bornik.cashlens.inbound;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class InboundService {

    private final InboundMessageRepository repository;

    void saveTextMessage(String payload) {
        repository.save(new InboundMessage(payload, InputSource.TEXT_MESSAGE));
    }

}
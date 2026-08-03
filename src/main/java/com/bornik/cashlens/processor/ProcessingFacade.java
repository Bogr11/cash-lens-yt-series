package com.bornik.cashlens.processor;

import com.bornik.cashlens.inbound.InboundMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessingFacade {

    private final ExpenseAiAssistant assistant;
    private final ExpenseRepository repository;

    @Transactional
    public void process(InboundMessageDto message) {
        var parsed = assistant.extract(message.payload());
        log.info("Parsed {} as {}", message, parsed);
        var saved = repository.save(new Expense(parsed));
        log.info("Saved {}", saved);
    }

}

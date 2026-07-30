package com.bornik.cashlens.processor;

import com.bornik.cashlens.inbound.InboundMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParserService {

    private final ExpenseAiAssistant assistant;
    private final ExpenseRepository repository;

    @Transactional
    public void parseAndSave(InboundMessageDto message) {
        ParsedExpense parsed = assistant.extract(message.payload());
        repository.save(new Expense(parsed));
    }

}

package com.bornik.cashlens.processor;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

interface ExpenseAiAssistant {

    String PROMPT = """
            You extract a single structured expense from a plain text expense description.

            """ + ExtractionRules.FIELDS;

    @SystemMessage(PROMPT)
    @UserMessage("{{it}}")
    ParsedExpense extract(String input);

}

package com.bornik.cashlens.processor;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

import static com.bornik.cashlens.processor.AiExtractionRules.COMMON_FIELD_RULES;

interface ExpenseAiAssistant {

    String EXTRACTION_PROMPT = """
            You extract a single structured expense from a plain text expense description.

            """ + COMMON_FIELD_RULES;

    @SystemMessage(EXTRACTION_PROMPT)
    @UserMessage("{{it}}")
    ParsedExpense extract(String input);

}

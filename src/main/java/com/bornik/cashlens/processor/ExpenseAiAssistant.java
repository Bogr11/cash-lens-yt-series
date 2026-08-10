package com.bornik.cashlens.processor;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * Reads a typed expense description. Text only — receipts are read by
 * {@link ReceiptAiAssistant}, which runs its own prompt and can run its own model.
 */
interface ExpenseAiAssistant {

    String PROMPT = """
            You extract a single structured expense from a plain text expense description.

            """ + ExtractionRules.FIELDS;

    @SystemMessage(PROMPT)
    @UserMessage("{{it}}")
    ParsedExpense extract(String input);

}

package com.bornik.cashlens.processor;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

interface ExpenseAiAssistant {

    String EXTRACTION_PROMPT = """
            You extract a single structured expense from a plain text expense description.

            FIELD RULES:
            - amount: numeric amount only, no currency symbol.
            - currency: ISO 4217 code (EUR, USD, GBP, …). Use EUR when the text does not state one.
            - category: one of — GROCERIES, TRANSPORT, EATING_OUT, UTILITIES, HEALTH, ENTERTAINMENT, OTHER.
            - description: one short sentence describing the expense, in the language of the input.
            - confidence: 0.0 to 1.0 — how sure you are that the fields above are right.

            Always fill every field with your best guess. When the text is vague, lower the
            confidence instead of leaving a field out.
            """;

    @SystemMessage(EXTRACTION_PROMPT)
    @UserMessage("{{it}}")
    ParsedExpense extract(String input);

}

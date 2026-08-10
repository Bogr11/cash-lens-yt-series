package com.bornik.cashlens.processor;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

interface ExpenseAiAssistant {

    String SHARED_FIELD_RULES = """
            FIELD RULES:
            - amount: numeric amount only, no currency symbol.
            - currency: ISO 4217 code (EUR, USD, GBP, …). Use EUR when nothing states one.
            - category: one of — GROCERIES, TRANSPORT, EATING_OUT, UTILITIES, HEALTH, ENTERTAINMENT, OTHER.
            - description: one short sentence describing the expense.
            - merchant: the shop or place, or null when it is not stated.
            - occurredAt: the date of the expense as YYYY-MM-DD, or null when it is not stated.
            - confidence: 0.0 to 1.0 — how sure you are that the fields above are right.

            Always fill amount, currency, category, description and confidence with your best
            guess. When you are unsure, lower the confidence instead of leaving a field out.
            Use null only for merchant and occurredAt, and only when they are genuinely absent.
            """;

    String EXTRACTION_PROMPT = """
            You extract a single structured expense from a plain text expense description.

            """ + SHARED_FIELD_RULES;

    String RECEIPT_PROMPT = """
            You extract a single structured expense from a photo of a receipt.

            READING RULES:
            - Read the whole image, including small print, the logo and the footer.
            - Take the TOTAL paid, not a line item and not the subtotal before tax.
            - Any language and locale. Normalise European-style numbers ("1.234,56" -> 1234.56).
            - Infer the currency from the symbol or the locale when it is not spelled out.
            - Crumpled, faded or partly unreadable receipts get a LOW confidence — that is
              what the field is for. Do not guess a total you cannot actually read.

            """ + SHARED_FIELD_RULES;

    @SystemMessage(EXTRACTION_PROMPT)
    @UserMessage("{{it}}")
    ParsedExpense extract(String input);

    /**
     * Both parameters are annotated: LangChain4j collects every Content-typed argument
     * into the user message, so the image travels alongside the instruction.
     */
    @SystemMessage(RECEIPT_PROMPT)
    ParsedExpense extractFromReceipt(@UserMessage String instruction, @UserMessage ImageContent receipt);

}

package com.bornik.cashlens.processor;

final class AiExtractionRules {

    static final String COMMON_FIELD_RULES = """
            FIELD RULES:
            - amount: numeric amount only, no currency symbol.
            - currency: ISO 4217 code (EUR, USD, GBP, …). Use EUR when the text does not state one.
            - category: one of — GROCERIES, TRANSPORT, EATING_OUT, UTILITIES, HEALTH, ENTERTAINMENT, OTHER, NOT_RECOGNIZED.
            - description: one short sentence describing the expense, in the language of the input.
            - merchant: the shop or place, or null when it is not stated.
            - confidence: 0.0 to 1.0 — how sure you are that the fields above are right.
            
            Always fill amount, currency, category, description and confidence with your best
            guess. When you are unsure, lower the confidence instead of leaving a field out.
            Use null only for merchant and occurredAt, and only when they are genuinely absent.
            """;

}

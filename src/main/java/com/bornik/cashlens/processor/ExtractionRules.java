package com.bornik.cashlens.processor;

final class ExtractionRules {

    static final String FIELDS = """
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

    private ExtractionRules() {
    }

}

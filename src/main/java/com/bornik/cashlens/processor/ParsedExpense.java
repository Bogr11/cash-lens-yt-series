package com.bornik.cashlens.processor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One contract for every carrier. Text and images are extracted into the same
 * record — merchant and occurredAt are nullable because a typed sentence often
 * does not carry them, while a receipt usually does.
 */
record ParsedExpense(BigDecimal amount,
                     String currency,
                     String category,
                     String description,
                     String merchant,
                     LocalDate occurredAt,
                     Double confidence) {
}

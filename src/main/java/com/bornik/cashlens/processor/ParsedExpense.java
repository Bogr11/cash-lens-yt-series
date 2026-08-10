package com.bornik.cashlens.processor;

import java.math.BigDecimal;
import java.time.LocalDate;

record ParsedExpense(BigDecimal amount,
                     String currency,
                     String category,
                     String description,
                     String merchant,
                     LocalDate occurredAt,
                     Double confidence) {
}

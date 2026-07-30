package com.bornik.cashlens.processor;

import java.math.BigDecimal;

record ParsedExpense(BigDecimal amount,
                     String currency,
                     String category,
                     String description,
                     Double confidence) {
}

package com.bornik.cashlens.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
class ExpenseController {

    private final ExpenseRepository repository;

    @GetMapping
    List<ExpenseView> all() {
        return repository.findAll().stream().map(ExpenseView::of).toList();
    }

    record ExpenseView(Long id, BigDecimal amount, String currency,
                       String category, String description,
                       Double confidence, Instant createdDate) {

        static ExpenseView of(Expense expense) {
            return new ExpenseView(expense.getId(),
                    expense.getAmount(),
                    expense.getCurrency(),
                    expense.getCategory(),
                    expense.getDescription(),
                    expense.getConfidence(),
                    expense.getCreatedDate());
        }

    }

}

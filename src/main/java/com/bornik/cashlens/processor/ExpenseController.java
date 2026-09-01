package com.bornik.cashlens.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
class ExpenseController {

    private static final String ACCOUNT = "X-Account-Id";

    private final ExpenseService expenseService;

    @GetMapping
    List<ExpenseView> all(@RequestHeader(ACCOUNT) String accountId, @RequestParam(defaultValue = "false") boolean needsReview) {

        var expenses = needsReview
                ? expenseService.findNeedingReview(accountId)
                : expenseService.findAll(accountId);

        return expenses.stream().map(ExpenseView::of).toList();
    }

    record ExpenseView(Long id, BigDecimal amount, String currency,
                       String category, String description, String merchant,
                       Double confidence, Instant createdDate) {

        static ExpenseView of(Expense expense) {
            return new ExpenseView(expense.getId(),
                    expense.getAmount(),
                    expense.getCurrency(),
                    expense.getCategory(),
                    expense.getDescription(),
                    expense.getMerchant(),
                    expense.getConfidence(),
                    expense.getCreatedDate());
        }

    }

}

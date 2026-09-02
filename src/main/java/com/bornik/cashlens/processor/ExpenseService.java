package com.bornik.cashlens.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
class ExpenseService {

    private final ExpenseRepository repository;

    private static final Predicate<Expense> LOW_CONFIDENCE = expense -> expense.getConfidence() < 0.6;
    private static final Predicate<Expense> NO_MERCHANT = expense -> !StringUtils.hasText(expense.getMerchant());

    private static final Predicate<Expense> NEEDS_REVIEW = LOW_CONFIDENCE.or(NO_MERCHANT);

    List<Expense> findNeedingReview(String accountId) {
        return repository.findByAccountId(accountId).stream()
                .filter(NEEDS_REVIEW)
                .toList();
    }

    List<Expense> findAll(String accountId) {
        return repository.findByAccountId(accountId);
    }

}

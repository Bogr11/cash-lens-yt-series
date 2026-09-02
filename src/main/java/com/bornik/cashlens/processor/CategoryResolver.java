package com.bornik.cashlens.processor;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
class CategoryResolver {

    private static final Category NOT_RECOGNIZED = new Category("NOT_RECOGNIZED");

    private final ExpenseRepository repository;

    Category resolve(String accountId, ParsedExpense parsed) {
        return getLatestExpense(accountId, parsed.merchant())
                .or(getCategoryFromLlm(parsed))
                .orElse(NOT_RECOGNIZED);
    }

    private static @NonNull Supplier<Optional<? extends Category>> getCategoryFromLlm(ParsedExpense parsed) {
        return () -> Optional.ofNullable(parsed.category()).map(Category::new);
    }

    private Optional<Category> getLatestExpense(String accountId, String merchant) {
        return Optional.ofNullable(merchant)
                .flatMap(m -> repository.findByAccountIdAndMerchantOrderByCreatedDateDesc(accountId, m))
                .map(Expense::getCategory)
                .map(Category::new);
    }

    record Category(String value) {}

}
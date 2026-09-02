package com.bornik.cashlens.processor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryResolverTest {

    @Mock
    private ExpenseRepository repository;

    @InjectMocks
    private CategoryResolver resolver;

    @Test
    void takesWhatTheSameMerchantWasCategorisedAsBefore() {
        knownAs("Some Cafe", "Eating Out");

        assertThat(resolver.resolve("acc_1", parsed("Groceries", "Some Cafe", 0.95)).value())
                .isEqualTo("Eating Out");
    }

    @Test
    void fallsBackToTheModelWhenTheMerchantIsNew() {
        noHistoryFor("Corner Bakery");

        assertThat(resolver.resolve("acc_1", parsed("Eating Out", "Corner Bakery", 0.95)).value())
                .isEqualTo("Eating Out");
    }

    @Test
    void takesTheModelEvenWhenItIsNotConfident() {
        noHistoryFor("Corner Bakery");

        assertThat(resolver.resolve("acc_1", parsed("Eating Out", "Corner Bakery", 0.2)).value())
                .isEqualTo("Eating Out");
    }

    @Test
    void doesNotLetOneUnrecognisedRowPoisonTheMerchantForever() {
        knownAs("Some Cafe", "NOT_RECOGNIZED");

        assertThat(resolver.resolve("acc_1", parsed("Eating Out", "Some Cafe", 0.95)).value())
                .isEqualTo("Eating Out");
    }

    @Test
    void saysNotRecognisedWhenTheModelGaveNoCategory() {
        noHistoryFor("Corner Bakery");

        assertThat(resolver.resolve("acc_1", parsed(null, "Corner Bakery", 0.95)).value())
                .isEqualTo("NOT_RECOGNIZED");
    }

    @Test
    void neverAsksTheDatabaseWithoutAMerchant() {
        resolver.resolve("acc_1", parsed("Groceries", null, 0.95));

        verify(repository, never())
                .findByAccountIdAndMerchantOrderByCreatedDateDesc(anyString(), anyString());
    }

    @Test
    void takesTheModelWhenThereIsNoMerchantToLookUp() {
        assertThat(resolver.resolve("acc_1", parsed("Groceries", null, 0.95)))
                .isEqualTo("Groceries");
    }

    private void knownAs(String merchant, String category) {
        when(repository.findByAccountIdAndMerchantOrderByCreatedDateDesc("acc_1", merchant))
                .thenReturn(Optional.of(existing(category)));
    }

    private void noHistoryFor(String merchant) {
        when(repository.findByAccountIdAndMerchantOrderByCreatedDateDesc("acc_1", merchant))
                .thenReturn(Optional.empty());
    }

    private ParsedExpense parsed(String category, String merchant, Double confidence) {
        return new ParsedExpense(new BigDecimal("4.50"), "EUR", category, "coffee", merchant, confidence);
    }

    private Expense existing(String category) {
        return new Expense("acc_1", parsed(category, "Some Cafe", 0.9), category);
    }

}

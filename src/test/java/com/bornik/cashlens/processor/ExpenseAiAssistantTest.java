package com.bornik.cashlens.processor;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ExpenseAiAssistantTest {

    @Autowired
    private ExpenseAiAssistant assistant;

    @ParameterizedTest
    @CsvSource(delimiter = ';', value = {
            "coffee and a croissant 185;     185;     EUR; EATING_OUT",
            "taxi to the airport 420;        420;     EUR; TRANSPORT",
            "paid the electricity bill 2450; 2450;    EUR; UTILITIES",
            "Netflix subscription 12.99 USD; 12.99;   USD; ENTERTAINMENT",
            "groceries at Lidl 1237.50 UAH;  1237.50; UAH; GROCERIES",
    })
    void extractsExpenseFromPayload(String payload, String amount, String currency, String category) {
        ParsedExpense parsed = assistant.extract(payload);

        assertThat(parsed.amount()).isEqualByComparingTo(amount);
        assertThat(parsed.currency()).isEqualTo(currency);
        assertThat(parsed.category()).isEqualTo(category);
        assertThat(parsed.description()).isNotBlank();
        assertThat(parsed.confidence()).isBetween(0.0, 1.0);
    }

}

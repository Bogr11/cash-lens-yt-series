package com.bornik.cashlens.processor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static com.bornik.cashlens.inbound.InboundMessages.textMessage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessingFacadeTest {

    @Mock
    private ExpenseAiAssistant assistant;

    @Mock
    private ExpenseRepository repository;

    @InjectMocks
    private ProcessingFacade processingFacade;

    @Test
    void savesParsedExpense() {
        given(new ParsedExpense(new BigDecimal("185.00"), "EUR", "EATING_OUT", "coffee", 0.95));

        processingFacade.process(textMessage("coffee 185"));

        Expense saved = captureSaved();
        assertThat(saved.getAmount()).isEqualByComparingTo("185.00");
        assertThat(saved.getCurrency()).isEqualTo("EUR");
        assertThat(saved.getCategory()).isEqualTo("EATING_OUT");
        assertThat(saved.getDescription()).isEqualTo("coffee");
        assertThat(saved.getConfidence()).isEqualTo(0.95);
        assertThat(saved.getCreatedDate()).isNotNull();
    }

    @Test
    void keepsLowConfidenceFromVagueInput() {
        given(new ParsedExpense(new BigDecimal("5"), "EUR", "OTHER", "unclear purchase", 0.2));

        processingFacade.process(textMessage("5 for that thing"));

        Expense saved = captureSaved();
        assertThat(saved.getConfidence()).isEqualTo(0.2);
        assertThat(saved.getCategory()).isEqualTo("OTHER");
    }

    private void given(ParsedExpense parsed) {
        when(assistant.extract(anyString())).thenReturn(parsed);
    }

    private Expense captureSaved() {
        ArgumentCaptor<Expense> saved = ArgumentCaptor.forClass(Expense.class);
        verify(repository).save(saved.capture());
        return saved.getValue();
    }

}

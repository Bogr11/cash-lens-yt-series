package com.bornik.cashlens.processor;

import dev.langchain4j.data.message.AudioContent;
import dev.langchain4j.data.message.ImageContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;

import static com.bornik.cashlens.inbound.InboundMessages.receipt;
import static com.bornik.cashlens.inbound.InboundMessages.textMessage;
import static com.bornik.cashlens.inbound.InboundMessages.voice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessingFacadeTest {

    private static final byte[] BYTES = "not really an image".getBytes(StandardCharsets.UTF_8);

    @Mock
    private ExpenseAiAssistant assistant;

    @Mock
    private ReceiptAiAssistant receiptAssistant;

    @Mock
    private VoiceAiAssistant voiceAssistant;

    @Mock
    private ExpenseRepository repository;

    @InjectMocks
    private ProcessingFacade processingFacade;

    // --- text ---

    @Test
    void savesParsedExpense() {
        when(assistant.extract(anyString()))
                .thenReturn(parsed("185.00", "EATING_OUT", "coffee", "Blue Bottle", LocalDate.of(2026, 8, 1), 0.95));

        processingFacade.process(textMessage("coffee 185"));

        Expense saved = captureSaved();
        assertThat(saved.getAmount()).isEqualByComparingTo("185.00");
        assertThat(saved.getCurrency()).isEqualTo("EUR");
        assertThat(saved.getCategory()).isEqualTo("EATING_OUT");
        assertThat(saved.getDescription()).isEqualTo("coffee");
        assertThat(saved.getMerchant()).isEqualTo("Blue Bottle");
        assertThat(saved.getOccurredAt()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(saved.getConfidence()).isEqualTo(0.95);
        assertThat(saved.getCreatedDate()).isNotNull();
    }

    @Test
    void keepsLowConfidenceFromVagueInput() {
        when(assistant.extract(anyString()))
                .thenReturn(parsed("5", "OTHER", "unclear purchase", null, null, 0.2));

        processingFacade.process(textMessage("5 for that thing"));

        assertThat(captureSaved().getConfidence()).isEqualTo(0.2);
    }

    @Test
    void leavesMerchantAndDateEmptyWhenTextDoesNotCarryThem() {
        when(assistant.extract(anyString()))
                .thenReturn(parsed("12.50", "TRANSPORT", "taxi home", null, null, 0.7));

        processingFacade.process(textMessage("taxi home 12.50"));

        Expense saved = captureSaved();
        assertThat(saved.getMerchant()).isNull();
        assertThat(saved.getOccurredAt()).isNull();
    }

    // --- photo ---

    @Test
    void sendsThePhotoToTheReceiptAssistantAndSavesWhatComesBack() {
        when(receiptAssistant.extract(any(ImageContent.class)))
                .thenReturn(parsed("22.75", "GROCERIES", "groceries", "Mercadona", LocalDate.of(2026, 8, 3), 0.95));

        processingFacade.process(receipt(BYTES, "receipt.jpg"));

        ArgumentCaptor<ImageContent> sent = ArgumentCaptor.forClass(ImageContent.class);
        verify(receiptAssistant).extract(sent.capture());
        assertThat(sent.getValue().image().base64Data()).isEqualTo(Base64.getEncoder().encodeToString(BYTES));
        assertThat(sent.getValue().image().mimeType()).isEqualTo("image/jpeg");

        assertThat(captureSaved().getMerchant()).isEqualTo("Mercadona");
        verifyNoInteractions(assistant, voiceAssistant);
    }

    @Test
    void derivesTheMimeTypeFromTheFileName() {
        when(receiptAssistant.extract(any(ImageContent.class)))
                .thenReturn(parsed("1", "OTHER", "x", null, null, 0.5));

        processingFacade.process(receipt(BYTES, "scan.png"));

        ArgumentCaptor<ImageContent> sent = ArgumentCaptor.forClass(ImageContent.class);
        verify(receiptAssistant).extract(sent.capture());
        assertThat(sent.getValue().image().mimeType()).isEqualTo("image/png");
    }

    // --- voice ---

    @Test
    void sendsTheVoiceNoteToTheVoiceAssistant() {
        when(voiceAssistant.extract(any(AudioContent.class)))
                .thenReturn(parsed("12.00", "EATING_OUT", "espresso and a sandwich", null, null, 0.9));

        processingFacade.process(voice(BYTES, "note.m4a"));

        ArgumentCaptor<AudioContent> sent = ArgumentCaptor.forClass(AudioContent.class);
        verify(voiceAssistant).extract(sent.capture());
        assertThat(sent.getValue().audio().mimeType()).isEqualTo("audio/mp4");

        assertThat(captureSaved().getAmount()).isEqualByComparingTo("12.00");
        verifyNoInteractions(assistant, receiptAssistant);
    }

    // --- guard ---

    @Test
    void refusesAFileMessageThatCarriesNoBytes() {
        assertThatThrownBy(() -> processingFacade.process(receipt(null, "receipt.jpg")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("receipt.jpg");

        verifyNoInteractions(receiptAssistant, repository);
    }

    private ParsedExpense parsed(String amount, String category, String description,
                                 String merchant, LocalDate occurredAt, double confidence) {
        return new ParsedExpense(new BigDecimal(amount), "EUR", category, description,
                merchant, occurredAt, confidence);
    }

    private Expense captureSaved() {
        ArgumentCaptor<Expense> saved = ArgumentCaptor.forClass(Expense.class);
        verify(repository).save(saved.capture());
        return saved.getValue();
    }

}

package com.bornik.cashlens.processor;

import dev.langchain4j.data.audio.Audio;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.AudioContent;
import dev.langchain4j.data.message.ImageContent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
class CarrierExtractionTest {

    private static final String RECEIPT = "receipt.jpg";
    private static final String VOICE = "voice.m4a";

    @Autowired
    private ReceiptAiAssistant receiptAiAssistant;

    @Autowired
    private VoiceAiAssistant voiceAiAssistant;

    @Test
    void readsAnExpenseFromAReceiptPhoto() {
        byte[] bytes = fixture(RECEIPT);
        assumeTrue(bytes != null, RECEIPT + " not in test resources");

        ImageContent receipt = ImageContent.from(Image.builder()
                .base64Data(Base64.getEncoder().encodeToString(bytes))
                .mimeType("image/jpeg")
                .build());

        ParsedExpense parsed = receiptAiAssistant.extract(receipt);

        assertThat(parsed.amount()).isPositive();
        assertThat(parsed.currency()).hasSize(3);
        assertThat(parsed.description()).isNotBlank();
        assertThat(parsed.confidence()).isBetween(0.0, 1.0);
    }

    @Test
    void readsAnExpenseFromAVoiceMessage() {
        byte[] bytes = fixture(VOICE);
        assumeTrue(bytes != null, VOICE + " not in test resources");

        AudioContent voice = AudioContent.from(Audio.builder()
                .base64Data(Base64.getEncoder().encodeToString(bytes))
                .mimeType("audio/mp4")
                .build());

        ParsedExpense parsed = voiceAiAssistant.extract(voice);

        assertThat(parsed.amount()).isPositive();
        assertThat(parsed.currency()).hasSize(3);
        assertThat(parsed.description()).isNotBlank();
        assertThat(parsed.confidence()).isBetween(0.0, 1.0);
    }

    private byte[] fixture(String name) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(name)) {
            return in == null ? null : in.readAllBytes();
        } catch (IOException e) {
            return null;
        }
    }

}

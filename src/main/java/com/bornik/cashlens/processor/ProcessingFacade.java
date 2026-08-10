package com.bornik.cashlens.processor;

import com.bornik.cashlens.inbound.InboundMessageDto;
import dev.langchain4j.data.audio.Audio;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.AudioContent;
import dev.langchain4j.data.message.ImageContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessingFacade {

    private static final String DEFAULT_IMAGE_TYPE = "image/jpeg";
    private static final String DEFAULT_AUDIO_TYPE = "audio/mp4";
    private static final String GENERIC_BINARY = "application/octet-stream";

    private final ExpenseAiAssistant expenseAiAssistant;
    private final ReceiptAiAssistant receiptAiAssistant;
    private final VoiceAiAssistant voiceAiAssistant;
    private final ExpenseRepository repository;

    @Transactional
    public void process(InboundMessageDto message) {
        ParsedExpense parsed = switch (message.source()) {
            case TEXT_MESSAGE -> expenseAiAssistant.extract(message.payload());
            case PHOTO -> extractFromReceipt(message);
            case VOICE_MESSAGE -> extractFromVoice(message);
        };

        log.info("Parsed {} as {}", message, parsed);
        Expense saved = repository.save(new Expense(parsed));
        log.info("Saved {}", saved);
    }

    private ParsedExpense extractFromReceipt(InboundMessageDto message) {
        return receiptAiAssistant.extract(ImageContent.from(Image.builder()
                .base64Data(base64Of(message))
                .mimeType(contentTypeOf(message, DEFAULT_IMAGE_TYPE))
                .build()));
    }

    private ParsedExpense extractFromVoice(InboundMessageDto message) {
        return voiceAiAssistant.extract(AudioContent.from(Audio.builder()
                .base64Data(base64Of(message))
                .mimeType(contentTypeOf(message, DEFAULT_AUDIO_TYPE))
                .build()));
    }

    private String base64Of(InboundMessageDto message) {
        byte[] content = message.content();
        if (content == null || content.length == 0) {
            throw new IllegalStateException(message.source() + " message carries no content");
        }
        return Base64.getEncoder().encodeToString(content);
    }

    private String contentTypeOf(InboundMessageDto message, String fallback) {
        String contentType = message.contentType();
        if (contentType == null || contentType.isBlank() || contentType.equals(GENERIC_BINARY)) {
            return fallback;
        }
        return contentType;
    }

}

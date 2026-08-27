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


    private final ReceiptAiAssistant receiptAiAssistant;
    private final VoiceAiAssistant voiceAiAssistant;
    private final ExpenseAiAssistant textMessageAssistant;
    private final ExpenseRepository repository;

    @Transactional
    public void process(InboundMessageDto message) {
        var parsed = parse(message);
        save(message.accountId(), parsed);
    }

    private ParsedExpense parse(InboundMessageDto message) {
        var parsed = switch (message.source()) {
            case TEXT_MESSAGE -> textMessageAssistant.extract(message.payload());
            case VOICE_MESSAGE -> fromVoice(message);
            case PHOTO -> fromPicture(message);
        };

        log.info("Parsed {} as {}", message, parsed);
        return parsed;
    }

    private void save(String accountId, ParsedExpense parsed) {
        var saved = repository.save(new Expense(accountId, parsed));
        log.info("Saved {}", saved);
    }

    private ParsedExpense fromVoice(InboundMessageDto message) {
        return voiceAiAssistant.extract(AudioContent.from(Audio.builder()
                .base64Data(base64(message))
                .mimeType(contentTypeOf(message, MimeTypes.DEFAULT_AUDIO_TYPE))
                .build()));
    }

    private ParsedExpense fromPicture(InboundMessageDto message) {
        return receiptAiAssistant.extract(ImageContent.from(Image.builder()
                .base64Data(base64(message))
                .mimeType(contentTypeOf(message, MimeTypes.DEFAULT_IMAGE_TYPE))
                .build()));
    }

    private String contentTypeOf(InboundMessageDto message, String fallback) {
        String contentType = message.contentType();
        if (contentType == null || contentType.isBlank() || contentType.equals(MimeTypes.GENERIC_BINARY)) {
            return fallback;
        }
        return contentType;
    }

    private String base64(InboundMessageDto message) {
        byte[] content = message.content();
        if (content == null || content.length == 0) {
            throw new IllegalStateException(message.source() + " message carries no content");
        }
        return Base64.getEncoder().encodeToString(content);
    }

    private static class MimeTypes {
        private static final String DEFAULT_IMAGE_TYPE = "image/jpeg";
        private static final String DEFAULT_AUDIO_TYPE = "audio/mp4";
        private static final String GENERIC_BINARY = "application/octet-stream";
    }

}

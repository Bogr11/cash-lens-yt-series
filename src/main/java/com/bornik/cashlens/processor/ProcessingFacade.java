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
import java.util.Map;

/**
 * The only public class of this package, with the only public method.
 * Nothing outside can reach the entity, the repository or the assistants.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessingFacade {

    private static final String DEFAULT_IMAGE_TYPE = "image/jpeg";
    private static final String DEFAULT_AUDIO_TYPE = "audio/mp4";

    private static final Map<String, String> MIME_TYPES = Map.of(
            ".png", "image/png",
            ".webp", "image/webp",
            ".heic", "image/heic",
            ".m4a", "audio/mp4",
            ".mp3", "audio/mpeg",
            ".ogg", "audio/ogg",
            ".wav", "audio/wav");

    private final ExpenseAiAssistant expenseAiAssistant;
    private final ReceiptAiAssistant receiptAiAssistant;
    private final VoiceAiAssistant voiceAiAssistant;
    private final ExpenseRepository repository;

    @Transactional
    public void process(InboundMessageDto message) {
        // Three carriers, three prompts, one record. Everything below is unaware.
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
        ImageContent receipt = ImageContent.from(Image.builder()
                .base64Data(base64Of(message))
                .mimeType(mimeTypeOf(message.payload(), DEFAULT_IMAGE_TYPE))
                .build());

        return receiptAiAssistant.extract(receipt);
    }

    private ParsedExpense extractFromVoice(InboundMessageDto message) {
        AudioContent voice = AudioContent.from(Audio.builder()
                .base64Data(base64Of(message))
                .mimeType(mimeTypeOf(message.payload(), DEFAULT_AUDIO_TYPE))
                .build());

        return voiceAiAssistant.extract(voice);
    }

    private String base64Of(InboundMessageDto message) {
        byte[] content = message.content();
        if (content == null || content.length == 0) {
            throw new IllegalStateException(
                    message.source() + " message " + message.payload() + " carries no content");
        }
        return Base64.getEncoder().encodeToString(content);
    }

    private String mimeTypeOf(String fileName, String fallback) {
        if (fileName == null) {
            return fallback;
        }
        String lower = fileName.toLowerCase();
        return MIME_TYPES.entrySet().stream()
                .filter(entry -> lower.endsWith(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(fallback);
    }

}

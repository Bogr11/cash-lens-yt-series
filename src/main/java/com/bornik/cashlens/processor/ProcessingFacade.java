package com.bornik.cashlens.processor;

import com.bornik.cashlens.inbound.InboundMessageDto;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.ImageContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

/**
 * The only public class of this package, with the only public method.
 * Nothing outside can reach the entity, the repository or the assistant.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessingFacade {

    private static final String DEFAULT_MIME_TYPE = "image/jpeg";
    private static final String RECEIPT_INSTRUCTION = "Extract the expense from this receipt.";

    private final ExpenseAiAssistant expenseAiAssistant;
    private final ReceiptAiAssistant receiptAiAssistant;
    private final ExpenseRepository repository;

    @Transactional
    public void process(InboundMessageDto message) {
        // Two different extractors, two different prompts, possibly two different
        // models — and one record. Everything below this switch is unaware.
        ParsedExpense parsed = switch (message.source()) {
            case TEXT_MESSAGE -> expenseAiAssistant.extract(message.payload());
            case PHOTO -> extractFromReceipt(message.content(), message.payload());
            case VOICE_MESSAGE -> throw new UnsupportedOperationException(
                    "Voice messages are not supported yet");
        };

        log.info("Parsed {} as {}", message, parsed);
        Expense saved = repository.save(new Expense(parsed));
        log.info("Saved {}", saved);
    }

    private ParsedExpense extractFromReceipt(byte[] content, String fileName) {
        if (content == null || content.length == 0) {
            throw new IllegalStateException("Photo message " + fileName + " carries no image");
        }

        ImageContent receipt = ImageContent.from(Image.builder()
                .base64Data(Base64.getEncoder().encodeToString(content))
                .mimeType(mimeTypeOf(fileName))
                .build());

        return receiptAiAssistant.extract(RECEIPT_INSTRUCTION, receipt);
    }

    private String mimeTypeOf(String fileName) {
        if (fileName == null) {
            return DEFAULT_MIME_TYPE;
        }
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        if (lower.endsWith(".heic")) {
            return "image/heic";
        }
        return DEFAULT_MIME_TYPE;
    }

}

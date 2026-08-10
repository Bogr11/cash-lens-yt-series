package com.bornik.cashlens.processor;

import com.bornik.cashlens.inbound.InboundMessageDto;
import dev.langchain4j.data.audio.Audio;
import dev.langchain4j.data.message.AudioContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        save(parsed);
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

    private ParsedExpense fromVoice(InboundMessageDto message) {
        return voiceAiAssistant.extract(AudioContent.from(Audio.builder()
                шт.base64Data()


        ));
    }

    private String base64(InboundMessageDto message) {
        byte[] content = message;
    }

    private ParsedExpense fromPicture(InboundMessageDto message) {
        return receiptAiAssistant.extract();
    }

    private void save(ParsedExpense parsed) {
        var saved = repository.save(new Expense(parsed));
        log.info("Saved {}", saved);
    }

}

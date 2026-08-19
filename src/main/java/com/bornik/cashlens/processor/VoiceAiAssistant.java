package com.bornik.cashlens.processor;

import dev.langchain4j.data.message.AudioContent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

import static com.bornik.cashlens.processor.AiExtractionRules.COMMON_FIELD_RULES;

interface VoiceAiAssistant {

    String PROMPT = """
            You extract a single structured expense from a spoken message.

            LISTENING RULES:
            - The speaker is describing something they paid for, casually and in one breath.
            - Amounts may be spoken as words rather than digits. Convert them to a number.
            - Any language. Infer the currency from what is said, or from the language.
            - Use only what you actually heard. Never take a number, a currency or a
              merchant from these instructions.
            - Mumbled, noisy or half-heard audio gets a LOW confidence — that is what the
              field is for. Do not invent an amount you did not actually hear.

            """ + COMMON_FIELD_RULES;

    @SystemMessage(PROMPT)
    @UserMessage("Extract the expense from this voice message")
    ParsedExpense extract(AudioContent voice);

}

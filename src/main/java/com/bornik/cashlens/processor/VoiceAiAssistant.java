package com.bornik.cashlens.processor;

import dev.langchain4j.data.message.AudioContent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * Reads a spoken expense. Mirror of {@link ReceiptAiAssistant} — same shape,
 * different carrier, same record out.
 * <p>
 * A transcribe-then-parse chain would work here, unlike with receipts: speech has
 * no layout to lose. It stays a single call anyway, because doing it the same way
 * as every other carrier is the point.
 */
interface VoiceAiAssistant {

    String PROMPT = """
            You extract a single structured expense from a spoken message.

            LISTENING RULES:
            - The speaker is describing something they paid for, casually and in one breath.
            - Numbers may be spoken in words ("forty seven fifty" -> 47.50).
            - Any language. Infer the currency from what is said, or from the language.
            - Mumbled, noisy or half-heard audio gets a LOW confidence — that is what the
              field is for. Do not invent an amount you did not actually hear.

            """ + ExtractionRules.FIELDS;

    @SystemMessage(PROMPT)
    @UserMessage("Extract the expense from this voice message.")
    ParsedExpense extract(AudioContent voice);

}

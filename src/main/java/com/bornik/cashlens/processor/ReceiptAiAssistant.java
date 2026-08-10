package com.bornik.cashlens.processor;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * Reads a photo of a receipt. Separate from the text extractor on purpose:
 * different prompt, different failure modes, and — because AiServices binds one
 * model per interface — the freedom to run this on a stronger vision model
 * while plain text stays on a cheap fast one.
 * <p>
 * Deliberately NOT a chain of "vision reads text -> text extractor parses it":
 * that costs two calls and throws away the layout, which is exactly what tells
 * the model apart a total from a line item.
 */
interface ReceiptAiAssistant {

    String PROMPT = """
            You extract a single structured expense from a photo of a receipt.

            READING RULES:
            - Read the whole image, including small print, the logo and the footer.
            - Take the TOTAL paid, not a line item and not the subtotal before tax.
            - Any language and locale. Normalise European-style numbers ("1.234,56" -> 1234.56).
            - Infer the currency from the symbol or the locale when it is not spelled out.
            - Crumpled, faded or partly unreadable receipts get a LOW confidence — that is
              what the field is for. Do not guess a total you cannot actually read.

            """ + ExtractionRules.FIELDS;

    /**
     * The image is the whole user message — LangChain4j appends every Content-typed
     * argument to it. No text parameter: there is nothing to say that the system
     * message does not already say.
     */
    @SystemMessage(PROMPT)
    @UserMessage("Extract the expense from this receipt.")
    ParsedExpense extract(ImageContent receipt);

}

package com.bornik.cashlens.processor;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

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

    @SystemMessage(PROMPT)
    @UserMessage("Extract the expense from this receipt.")
    ParsedExpense extract(ImageContent receipt);

}

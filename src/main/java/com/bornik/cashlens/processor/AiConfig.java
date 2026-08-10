package com.bornik.cashlens.processor;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("processorAiModelConfiguration")
class AiConfig {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String textModel;

    @Value("${gemini.vision-model}")
    private String visionModel;

    @Bean
    ChatModel geminiChatModel() {
        return model(textModel);
    }

    /**
     * A second model bean, because AiServices binds one model per interface.
     * Today both point at the same Gemini; the moment receipts need a stronger
     * model than plain text, only this line changes.
     */
    @Bean
    ChatModel geminiVisionChatModel() {
        return model(visionModel);
    }

    @Bean
    ExpenseAiAssistant expenseAiAssistant(ChatModel geminiChatModel) {
        return AiServices.builder(ExpenseAiAssistant.class)
                .chatModel(geminiChatModel)
                .build();
    }

    @Bean
    ReceiptAiAssistant receiptAiAssistant(ChatModel geminiVisionChatModel) {
        return AiServices.builder(ReceiptAiAssistant.class)
                .chatModel(geminiVisionChatModel)
                .build();
    }

    private ChatModel model(String modelName) {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(0.0)
                .logRequestsAndResponses(true)
                .build();
    }

}

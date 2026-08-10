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

    @Value("${gemini.multimodal-model}")
    private String multimodalModel;

    @Bean
    ChatModel geminiChatModel() {
        return model(textModel);
    }

    /**
     * A second model bean, because AiServices binds one model per interface.
     * Receipts and voice notes share it — both are just non-text carriers.
     * Today it points at the same Gemini as plain text; when that changes,
     * only this line does.
     */
    @Bean
    ChatModel geminiMultimodalChatModel() {
        return model(multimodalModel);
    }

    @Bean
    ExpenseAiAssistant expenseAiAssistant(ChatModel geminiChatModel) {
        return AiServices.builder(ExpenseAiAssistant.class)
                .chatModel(geminiChatModel)
                .build();
    }

    @Bean
    ReceiptAiAssistant receiptAiAssistant(ChatModel geminiMultimodalChatModel) {
        return AiServices.builder(ReceiptAiAssistant.class)
                .chatModel(geminiMultimodalChatModel)
                .build();
    }

    @Bean
    VoiceAiAssistant voiceAiAssistant(ChatModel geminiMultimodalChatModel) {
        return AiServices.builder(VoiceAiAssistant.class)
                .chatModel(geminiMultimodalChatModel)
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

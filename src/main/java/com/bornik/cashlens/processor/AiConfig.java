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
    private String model;

    @Value("${gemini.multimodal-model}")
    private String multimodalModel;

    @Bean
    ChatModel geminiChatModel() {
        return createModel(model);
    }

    @Bean
    ChatModel geminiMultimodalModel() {
        return createModel(multimodalModel);
    }

    private ChatModel createModel(String modelName) {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(0.0)
                .logRequestsAndResponses(true)
                .build();
    }

    @Bean
    ExpenseAiAssistant expenseAiAssistant() {
        return AiServices.builder(ExpenseAiAssistant.class)
                .chatModel(geminiChatModel())
                .build();
    }

    @Bean
    ReceiptAiAssistant receiptAiAssistant() {
        return AiServices.builder(ReceiptAiAssistant.class)
                .chatModel(geminiMultimodalModel())
                .build();
    }

    @Bean
    VoiceAiAssistant voiceAiAssistant() {
        return AiServices.builder(VoiceAiAssistant.class)
                .chatModel(geminiChatModel())
                .build();
    }

}

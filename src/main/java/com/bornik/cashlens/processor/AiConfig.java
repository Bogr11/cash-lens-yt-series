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

    @Bean
    ChatModel geminiChatModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName(model)
                .temperature(0.0)
                .logRequestsAndResponses(true)
                .build();
    }

    @Bean
    ExpenseAiAssistant expenseAiAssistant(ChatModel geminiChatModel) {
        return AiServices.builder(ExpenseAiAssistant.class)
                .chatModel(geminiChatModel)
                .build();
    }

}

package com.example.robotwebsite.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    private final ChatClient chatClient;

    // ChatClient.Builderは自動でインジェクションされます
    public AiService(ChatClient.Builder builder) {
        this.chatClient = builder
            .defaultSystem("あなたは囲碁イベントの専門家です。必ず日本語で回答してください。")
            .build();
    }

    public String getAiResponse(String message) {
        String enhancedMessage = message + "\n\n必ず日本語で回答してください。";
        return this.chatClient.prompt()
                .user(enhancedMessage)
                .call()
                .content();
    }
}

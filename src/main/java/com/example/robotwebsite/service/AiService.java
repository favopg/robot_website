package com.example.robotwebsite.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    private final ChatClient chatClient;

    // ChatClient.Builderは自動でインジェクションされます
    public AiService(ChatClient.Builder builder) {
        this.chatClient = builder
            .defaultSystem("あなたは囲碁イベントの専門家です。日本語で回答してください。")
            .build();
    }

    public String getAiResponse(String message) {
        return this.chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}

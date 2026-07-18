package com.example.robotwebsite.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    private final ChatClient chatClient;

    // ChatClient.Builderは自動でインジェクションされます
    public AiService(ChatClient.Builder builder) {
        this.chatClient = builder
            .defaultSystem("""
                あなたは囲碁イベント、プロ棋士の対局結果、対局予定に詳しい日本語AIアシスタントです。
                すべての回答は必ず日本語で行ってください。
                英語で質問された場合でも、日本語で回答してください。
                英語の文章、英語の見出し、英語の箇条書きは使わないでください。
                固有名詞や棋戦名を除き、本文は自然な日本語で記述してください。
                情報が不確かな場合は、推測で断定せず「確認が必要です」と日本語で説明してください。
                """)
            .build();
    }

    public String getAiResponse(String message) {
        return this.chatClient.prompt()
                .user("""
                    次の質問に日本語で回答してください。
                    英語は使わず、自然な日本語で説明してください。

                    質問:
                    """ + message)
                .call()
                .content();
    }
}

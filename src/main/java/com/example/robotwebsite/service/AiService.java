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

    public record EventAttributes(boolean targetBeginner, boolean targetKyuPlayer, boolean targetDanPlayer) {}

    public EventAttributes extractEventAttributes(String description) {
        try {
            String response = this.chatClient.prompt()
                    .user("""
                        以下の囲碁イベントの説明文を分析し、対象者を判定してください。
                        結果は必ず以下のJSON形式のみで回答してください。他の文章は一切含めないでください。
                        {"targetBeginner": boolean, "targetKyuPlayer": boolean, "targetDanPlayer": boolean}

                        - targetBeginner: 入門者、初心者、これから始める人向けであれば true
                        - targetKyuPlayer: 級位者、1級〜30級向けであれば true
                        - targetDanPlayer: 有段者、初段以上、高段者向けであれば true

                        説明文:
                        """ + description)
                    .call()
                    .content();

            // 簡易的なJSONパース（本来はJackson等を使うべきだが、LLMの出力を安定させるために正規表現や文字列操作でも可）
            // Spring AIの機能で型指定して受け取ることも可能
            boolean beginner = response.contains("\"targetBeginner\": true");
            boolean kyu = response.contains("\"targetKyuPlayer\": true");
            boolean dan = response.contains("\"targetDanPlayer\": true");

            return new EventAttributes(beginner, kyu, dan);
        } catch (Exception e) {
            return new EventAttributes(false, false, false);
        }
    }
}

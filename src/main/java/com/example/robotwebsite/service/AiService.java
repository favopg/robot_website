package com.example.robotwebsite.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    private final ChatClient chatClient;

    // ChatClient.Builderは自動でインジェクションされます
    public AiService() {
        this.chatClient = null;
    }

    public String getAiResponse(String message) {
        if (this.chatClient == null) {
            return "現在、AI機能は停止しています。";
        }
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
        if (this.chatClient == null) {
            return new EventAttributes(false, false, false);
        }
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

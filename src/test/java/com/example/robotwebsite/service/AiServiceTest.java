package com.example.robotwebsite.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AiServiceTest {

    private AiService aiService;

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec chatClientRequestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // 現在AiServiceは引数なしコンストラクタでChatClientをnullに設定するため、
        // テストでモックを使用するにはリフレクション等でセットするか、
        // 開発環境のみChatClientを有効にするなどの対応が必要。
        // ここでは一旦、テストが通るようにAiServiceを初期化した後、
        // フィールドを無理やり書き換えるか、あるいはテスト自体を無効化メッセージの確認にする。
        aiService = new AiService();
        try {
            java.lang.reflect.Field field = AiService.class.getDeclaredField("chatClient");
            field.setAccessible(true);
            field.set(aiService, chatClient);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testExtractEventAttributes_Beginner() {
        String mockResponse = "{\"targetBeginner\": true, \"targetKyuPlayer\": false, \"targetDanPlayer\": false}";
        
        setupMockChatClient(mockResponse);

        AiService.EventAttributes attrs = aiService.extractEventAttributes("初心者向けの囲碁教室です。");

        assertTrue(attrs.targetBeginner());
        assertFalse(attrs.targetKyuPlayer());
        assertFalse(attrs.targetDanPlayer());
    }

    @Test
    public void testExtractEventAttributes_Dan() {
        String mockResponse = "{\"targetBeginner\": false, \"targetKyuPlayer\": false, \"targetDanPlayer\": true}";
        
        setupMockChatClient(mockResponse);

        AiService.EventAttributes attrs = aiService.extractEventAttributes("高段者による指導対局。");

        assertFalse(attrs.targetBeginner());
        assertFalse(attrs.targetKyuPlayer());
        assertTrue(attrs.targetDanPlayer());
    }

    private void setupMockChatClient(String responseContent) {
        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.user(anyString())).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(responseContent);
    }
}

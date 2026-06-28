package com.example.robotwebsite;

import com.example.robotwebsite.service.AiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AiChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiService aiService;

    @Test
    public void testGetSpringAiPage() throws Exception {
        mockMvc.perform(get("/spring-ai"))
                .andExpect(status().isOk())
                .andExpect(view().name("ai_chat"));
    }

    @Test
    public void testAskAi() throws Exception {
        String mockResponse = "AIからの回答です。";
        when(aiService.getAiResponse(anyString())).thenReturn(mockResponse);

        mockMvc.perform(post("/spring-ai/ask")
                .param("prompt", "テストプロンプト"))
                .andExpect(status().isOk())
                .andExpect(view().name("ai_chat"))
                .andExpect(model().attribute("prompt", "テストプロンプト"))
                .andExpect(model().attribute("response", mockResponse))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(mockResponse)));
    }

    @Test
    public void testAskAiError() throws Exception {
        when(aiService.getAiResponse(anyString())).thenThrow(new RuntimeException("AI Error"));

        mockMvc.perform(post("/spring-ai/ask")
                .param("prompt", "エラーのテスト"))
                .andExpect(status().isOk())
                .andExpect(view().name("ai_chat"))
                .andExpect(model().attributeExists("error"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("AIからの応答取得中にエラーが発生しました")));
    }
}

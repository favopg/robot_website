package com.example.robotwebsite;

import com.example.robotwebsite.service.AiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AiChatController {

    private static final Logger logger = LoggerFactory.getLogger(AiChatController.class);
    private final AiService aiService;

    @Autowired
    public AiChatController(AiService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/spring-ai")
    public String index() {
        return "ai_chat";
    }

    @PostMapping("/spring-ai/ask")
    public String ask(@RequestParam("prompt") String prompt, Model model) {
        logger.info("AI ask request received with prompt: {}", prompt);
        try {
            String response = aiService.getAiResponse(prompt);
            logger.info("AI response received successfully");
            model.addAttribute("prompt", prompt);
            model.addAttribute("response", response);
        } catch (Exception e) {
            logger.error("Error occurred while getting AI response", e);
            model.addAttribute("prompt", prompt);
            model.addAttribute("error", "AIからの応答取得中にエラーが発生しました: " + e.getMessage());
        }
        return "ai_chat";
    }
}

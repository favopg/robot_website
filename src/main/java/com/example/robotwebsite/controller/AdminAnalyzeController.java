package com.example.robotwebsite.controller;

import com.example.robotwebsite.dto.KatagoAnalyzeRequest;
import com.example.robotwebsite.service.KatagoAnalyzeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/admin/analyze")
public class AdminAnalyzeController {

    private static final Logger logger = LoggerFactory.getLogger(AdminAnalyzeController.class);

    private final KatagoAnalyzeService katagoAnalyzeService;
    private final ObjectMapper objectMapper;

    public AdminAnalyzeController(KatagoAnalyzeService katagoAnalyzeService, ObjectMapper objectMapper) {
        this.katagoAnalyzeService = katagoAnalyzeService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("turnRange", "0-50");
        model.addAttribute("maxVisits", 100);
        return "admin/analyze";
    }

    @PostMapping
    public String analyze(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "sgf_path", required = false) String sgfPath,
            @RequestParam(value = "sgf_content", required = false) String sgfContent,
            @RequestParam(value = "turn_range", required = false) String turnRange,
            @RequestParam(value = "max_visits", required = false, defaultValue = "100") Integer maxVisits,
            Model model) {

        model.addAttribute("sgfPath", sgfPath);
        model.addAttribute("sgfContent", sgfContent);
        model.addAttribute("turnRange", turnRange);
        model.addAttribute("maxVisits", maxVisits);

        String content = sgfContent;
        if (file != null && !file.isEmpty()) {
            try {
                content = new String(file.getBytes(), StandardCharsets.UTF_8);
                model.addAttribute("fileName", file.getOriginalFilename());
            } catch (IOException e) {
                logger.error("Failed to read uploaded SGF file", e);
                model.addAttribute("errorMessage", "SGFファイルの読み込みに失敗しました: " + e.getMessage());
                return "admin/analyze";
            }
        }

        if ((content == null || content.trim().isEmpty()) && (sgfPath == null || sgfPath.trim().isEmpty())) {
            model.addAttribute("errorMessage", "SGFファイル、SGFパス、またはSGF内容のいずれかを指定してください。");
            return "admin/analyze";
        }

        KatagoAnalyzeRequest request = new KatagoAnalyzeRequest();
        if (sgfPath != null && !sgfPath.trim().isEmpty()) {
            request.setSgfPath(sgfPath.trim());
        }
        if (content != null && !content.trim().isEmpty()) {
            request.setSgfContent(content);
        }
        request.setTurnRange(turnRange != null && !turnRange.trim().isEmpty() ? turnRange.trim() : null);
        request.setMaxVisits(maxVisits);

        try {
            String jsonResult = katagoAnalyzeService.analyze(request);
            String formattedJson = jsonResult;
            try {
                Object jsonObject = objectMapper.readValue(jsonResult, Object.class);
                formattedJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
            } catch (Exception e) {
                // If parsing fails, use the raw response
            }
            model.addAttribute("resultJson", formattedJson);
            model.addAttribute("successMessage", "解析が完了しました。");
        } catch (Exception e) {
            logger.error("KataGo analysis request failed", e);
            model.addAttribute("errorMessage", "KataGo解析APIの呼び出しに失敗しました: " + e.getMessage());
        }

        return "admin/analyze";
    }
}

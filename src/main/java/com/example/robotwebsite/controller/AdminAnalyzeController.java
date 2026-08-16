package com.example.robotwebsite.controller;

import com.example.robotwebsite.dto.KatagoAnalyzeRequest;
import com.example.robotwebsite.service.KatagoAnalyzeService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AdminAnalyzeController {

    private static final Logger logger = LoggerFactory.getLogger(AdminAnalyzeController.class);

    private final KatagoAnalyzeService katagoAnalyzeService;
    private final ObjectMapper objectMapper;

    public AdminAnalyzeController(KatagoAnalyzeService katagoAnalyzeService, ObjectMapper objectMapper) {
        this.katagoAnalyzeService = katagoAnalyzeService;
        this.objectMapper = objectMapper;
    }

    @GetMapping({"/admin/analyze", "/analyze"})
    public String index(Model model) {
        return "admin/analyze";
    }

    @GetMapping({"/admin/analyze/api", "/admin/analyze/data", "/analyze/api", "/analyze/data", "/recommended-kifu/api", "/recommended-kifu/data", "/recommend-kifu/api", "/recommend-kifu/data"})
    @ResponseBody
    public ResponseEntity<Map<String, Object>> analyzeApi() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        KatagoAnalyzeRequest request = new KatagoAnalyzeRequest(today);
        Map<String, Object> response = new HashMap<>();

        try {
            String jsonResult = katagoAnalyzeService.analyze(request);
            String formattedJson = jsonResult;
            String sgfContent = null;
            try {
                JsonNode jsonNode = objectMapper.readTree(jsonResult);
                formattedJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
                
                if (jsonNode.hasNonNull("sgf_content")) {
                    sgfContent = jsonNode.get("sgf_content").asText();
                } else if (jsonNode.hasNonNull("sgf")) {
                    sgfContent = jsonNode.get("sgf").asText();
                } else if (jsonNode.isArray() && jsonNode.size() > 0) {
                    for (JsonNode elem : jsonNode) {
                        if (elem.hasNonNull("sgf_content")) {
                            sgfContent = elem.get("sgf_content").asText();
                            break;
                        } else if (elem.hasNonNull("sgf")) {
                            sgfContent = elem.get("sgf").asText();
                            break;
                        }
                    }
                }

                if (sgfContent == null && jsonNode.hasNonNull("sgf_file_name")) {
                    String sgfFileName = jsonNode.get("sgf_file_name").asText();
                    sgfContent = loadSgfFromFile(sgfFileName);
                } else if (sgfContent == null && jsonNode.isArray() && jsonNode.size() > 0) {
                    for (JsonNode elem : jsonNode) {
                        if (elem.hasNonNull("sgf_file_name")) {
                            sgfContent = loadSgfFromFile(elem.get("sgf_file_name").asText());
                            if (sgfContent != null) break;
                        }
                    }
                }
            } catch (Exception e) {
                // If parsing fails, use the raw response
            }

            response.put("success", true);
            response.put("resultJson", formattedJson);
            response.put("sgfContent", sgfContent);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("KataGo analysis request failed", e);
            response.put("success", false);
            response.put("errorMessage", "KataGo解析APIの呼び出しに失敗しました: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private String loadSgfFromFile(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return null;
        }
        String trimmed = fileName.trim();

        // 探索候補パス
        Path[] candidatePaths = new Path[]{
                Paths.get(trimmed),
                Paths.get("engines", "sgf", trimmed),
                Paths.get("engines", "sgf", Paths.get(trimmed).getFileName().toString()),
                Paths.get("engines", trimmed),
                Paths.get("data", trimmed)
        };

        for (Path path : candidatePaths) {
            try {
                if (Files.exists(path) && !Files.isDirectory(path)) {
                    return Files.readString(path, StandardCharsets.UTF_8);
                }
            } catch (Exception e) {
                logger.warn("Failed to read SGF file at path: {}", path, e);
            }
        }

        // クラスパスからの読み込み試行
        try {
            org.springframework.core.io.ClassPathResource resource = new org.springframework.core.io.ClassPathResource(trimmed);
            if (resource.exists()) {
                return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            logger.warn("Failed to read SGF file from classpath: {}", trimmed, e);
        }

        logger.warn("SGF file not found for sgf_file_name: {}", fileName);
        return null;
    }
}

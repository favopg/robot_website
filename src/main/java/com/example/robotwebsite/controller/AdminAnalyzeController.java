package com.example.robotwebsite.controller;

import com.example.robotwebsite.dto.KatagoAnalyzeRequest;
import com.example.robotwebsite.service.KatagoAnalyzeService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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

                // results 配列が存在する場合、turn 昇順にソートし、blackWinrate/whiteWinrate を付与
                if (jsonNode.hasNonNull("results") && jsonNode.get("results").isArray()) {
                    List<JsonNode> resultsList = new ArrayList<>();
                    for (JsonNode item : jsonNode.get("results")) {
                        resultsList.add(item);
                    }

                    // 1. turn 昇順にソート
                    resultsList.sort(Comparator.comparingInt(a -> {
                        if (a.hasNonNull("turn")) return a.get("turn").asInt();
                        if (a.hasNonNull("turnNumber")) return a.get("turnNumber").asInt();
                        if (a.hasNonNull("moveNumber")) return a.get("moveNumber").asInt();
                        if (a.hasNonNull("move") && a.get("move").isInt()) return a.get("move").asInt();
                        return 0;
                    }));

                    // 2. blackWinrate / whiteWinrate の明示的な定義
                    ArrayNode sortedResults = objectMapper.createArrayNode();
                    for (JsonNode item : resultsList) {
                        if (item instanceof ObjectNode) {
                            ObjectNode node = (ObjectNode) item.deepCopy();
                            int turn = 0;
                            if (node.hasNonNull("turn")) turn = node.get("turn").asInt();
                            else if (node.hasNonNull("turnNumber")) turn = node.get("turnNumber").asInt();
                            else if (node.hasNonNull("moveNumber")) turn = node.get("moveNumber").asInt();

                            String player = "B";
                            if (node.hasNonNull("player")) {
                                player = node.get("player").asText().trim().toUpperCase();
                            } else if (node.has("rootInfo") && node.get("rootInfo").hasNonNull("currentPlayer")) {
                                player = node.get("rootInfo").get("currentPlayer").asText().trim().toUpperCase();
                            } else if (node.hasNonNull("currentPlayer")) {
                                player = node.get("currentPlayer").asText().trim().toUpperCase();
                            } else {
                                player = (turn % 2 == 1) ? "B" : "W";
                            }
                            if ("BLACK".equals(player)) player = "B";
                            if ("WHITE".equals(player)) player = "W";

                            double rawWinrate = 0.0;
                            if (node.hasNonNull("winrate")) {
                                rawWinrate = node.get("winrate").asDouble();
                            } else if (node.hasNonNull("winRate")) {
                                rawWinrate = node.get("winRate").asDouble();
                            } else if (node.has("rootInfo") && node.get("rootInfo").hasNonNull("winrate")) {
                                rawWinrate = node.get("rootInfo").get("winrate").asDouble();
                            }

                            double winratePct = Math.max(0.0, Math.min(100.0, rawWinrate));

                            double blackWinrate = "B".equals(player) ? winratePct : (100.0 - winratePct);
                            double whiteWinrate = "W".equals(player) ? winratePct : (100.0 - winratePct);

                            node.put("blackWinrate", Math.round(blackWinrate * 100.0) / 100.0);
                            node.put("whiteWinrate", Math.round(whiteWinrate * 100.0) / 100.0);

                            sortedResults.add(node);
                        } else {
                            sortedResults.add(item);
                        }
                    }

                    if (jsonNode instanceof ObjectNode) {
                        ((ObjectNode) jsonNode).set("results", sortedResults);
                    }
                }

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

                String[] matchNameKeys = {"match_name", "matchName", "tournament_name", "tournamentName", "tournament", "event_name", "eventName", "event", "game_name", "gameName", "title"};
                for (String k : matchNameKeys) {
                    if (jsonNode.hasNonNull(k)) {
                        response.put("matchName", jsonNode.get(k).asText());
                        break;
                    }
                }
                String[] blackPlayerKeys = {"black_player", "blackPlayer", "player_black", "playerBlack", "player1_name", "player1Name", "player1", "black", "pb"};
                for (String k : blackPlayerKeys) {
                    if (jsonNode.hasNonNull(k)) {
                        response.put("blackPlayer", jsonNode.get(k).asText());
                        break;
                    }
                }
                String[] whitePlayerKeys = {"white_player", "whitePlayer", "player_white", "playerWhite", "player2_name", "player2Name", "player2", "white", "pw"};
                for (String k : whitePlayerKeys) {
                    if (jsonNode.hasNonNull(k)) {
                        response.put("whitePlayer", jsonNode.get(k).asText());
                        break;
                    }
                }
                String[] resultKeys = {"result", "game_result", "gameResult", "winner_name", "winnerName", "winner", "re"};
                for (String k : resultKeys) {
                    if (jsonNode.hasNonNull(k)) {
                        response.put("result", jsonNode.get(k).asText());
                        break;
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

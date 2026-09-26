package com.example.robotwebsite.service;

import com.example.robotwebsite.dto.KatagoAnalyzeRequest;
import com.example.robotwebsite.dto.KifuInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class KatagoAnalyzeService {

    private static final Logger logger = LoggerFactory.getLogger(KatagoAnalyzeService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${analyze.cache.dir:C:/analyze_cache}")
    private String cacheBaseDir;

    /**
     * キャッシュディレクトリ内に存在する棋譜ファイルの日付一覧(YYYYMMDD)を降順で取得
     */
    public List<String> getAvailableDates() {
        TreeSet<String> dateSet = new TreeSet<>(Comparator.reverseOrder());
        Path base = Paths.get(cacheBaseDir);

        if (!Files.exists(base) || !Files.isDirectory(base)) {
            return new ArrayList<>(dateSet);
        }

        try {
            // 1. すべてのサブディレクトリ (YYYYMM等) 内を走査
            try (DirectoryStream<Path> subDirs = Files.newDirectoryStream(base, Files::isDirectory)) {
                for (Path subDir : subDirs) {
                    try (DirectoryStream<Path> files = Files.newDirectoryStream(subDir, "*.json")) {
                        for (Path file : files) {
                            extractDateFromFileName(file.getFileName().toString(), dateSet);
                        }
                    } catch (IOException ignored) {}
                }
            }
            // 2. キャッシュディレクトリ直下も走査
            try (DirectoryStream<Path> directFiles = Files.newDirectoryStream(base, "*.json")) {
                for (Path file : directFiles) {
                    extractDateFromFileName(file.getFileName().toString(), dateSet);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to scan available dates in cache dir: {}", base, e);
        }

        return new ArrayList<>(dateSet);
    }

    /**
     * 保持している解析済み棋譜のメタ情報一覧（日付降順）を取得
     */
    public List<KifuInfo> getAvailableKifuList() {
        List<String> dates = getAvailableDates();
        List<KifuInfo> kifuList = new ArrayList<>();

        for (String dateStr : dates) {
            try {
                KatagoAnalyzeRequest req = new KatagoAnalyzeRequest(dateStr);
                String jsonResult = analyze(req);
                JsonNode root = objectMapper.readTree(jsonResult);

                // SGF文字列の取得（root または 配列の要素）
                String sgfContent = extractSgfContent(root);

                String matchName = null;
                String blackPlayer = null;
                String whitePlayer = null;
                String result = null;

                // SGFタグからの抽出 (PB, BR, PW, WR, EV/GN, RE)
                if (sgfContent != null && !sgfContent.isEmpty()) {
                    String ev = extractPropertyFromSgf(sgfContent, "EV");
                    String gn = extractPropertyFromSgf(sgfContent, "GN");
                    String pb = extractPropertyFromSgf(sgfContent, "PB");
                    String br = extractPropertyFromSgf(sgfContent, "BR");
                    String pw = extractPropertyFromSgf(sgfContent, "PW");
                    String wr = extractPropertyFromSgf(sgfContent, "WR");
                    String re = extractPropertyFromSgf(sgfContent, "RE");

                    if (ev != null && !ev.isEmpty()) {
                        matchName = ev;
                    } else if (gn != null && !gn.isEmpty()) {
                        matchName = gn;
                    }

                    if (pb != null && !pb.isEmpty()) {
                        blackPlayer = pb + (br != null && !br.isEmpty() ? (" " + br) : "");
                    }
                    if (pw != null && !pw.isEmpty()) {
                        whitePlayer = pw + (wr != null && !wr.isEmpty() ? (" " + wr) : "");
                    }
                    if (re != null && !re.isEmpty()) {
                        result = formatResultString(re);
                    }
                }

                // JSONのフィールドからのフォールバック抽出
                if (matchName == null || matchName.isEmpty()) {
                    matchName = extractField(root, "match_name", "matchName", "tournament_name", "game_name", "title");
                }
                if (blackPlayer == null || blackPlayer.isEmpty()) {
                    String pb = extractField(root, "black_player", "blackPlayer", "player_black", "player1_name", "player1", "black", "pb");
                    String br = extractField(root, "black_rank", "blackRank", "player_black_rank", "br");
                    if (pb != null && !pb.isEmpty()) {
                        blackPlayer = pb + (br != null && !br.isEmpty() ? (" " + br) : "");
                    }
                }
                if (whitePlayer == null || whitePlayer.isEmpty()) {
                    String pw = extractField(root, "white_player", "whitePlayer", "player_white", "player2_name", "player2", "white", "pw");
                    String wr = extractField(root, "white_rank", "whiteRank", "player_white_rank", "wr");
                    if (pw != null && !pw.isEmpty()) {
                        whitePlayer = pw + (wr != null && !wr.isEmpty() ? (" " + wr) : "");
                    }
                }
                if (result == null || result.isEmpty()) {
                    String rawResult = extractField(root, "result", "game_result", "winner_name", "re");
                    result = formatResultString(rawResult);
                }

                kifuList.add(new KifuInfo(dateStr, matchName, blackPlayer, whitePlayer, result));
            } catch (Exception e) {
                logger.warn("Failed to extract kifu info for date: {}", dateStr, e);
                kifuList.add(new KifuInfo(dateStr, "棋譜 (" + dateStr + ")", "黒番", "白番", ""));
            }
        }
        return kifuList;
    }

    private String extractSgfContent(JsonNode node) {
        if (node == null) return null;
        if (node.hasNonNull("sgf_content")) return node.get("sgf_content").asText();
        if (node.hasNonNull("sgfContent")) return node.get("sgfContent").asText();
        if (node.hasNonNull("sgf")) return node.get("sgf").asText();

        if (node.isArray() && node.size() > 0) {
            for (JsonNode elem : node) {
                if (elem.hasNonNull("sgf_content")) return elem.get("sgf_content").asText();
                if (elem.hasNonNull("sgfContent")) return elem.get("sgfContent").asText();
                if (elem.hasNonNull("sgf")) return elem.get("sgf").asText();
            }
        }
        return null;
    }

    private String extractPropertyFromSgf(String sgf, String propertyName) {
        if (sgf == null || propertyName == null) return null;
        Pattern pattern = Pattern.compile("(?:^|[^A-Z])" + Pattern.quote(propertyName) + "\\[([^\\]]*(?:\\\\\\][^\\]]*)*)\\]", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sgf);
        if (matcher.find() && matcher.group(1) != null) {
            return matcher.group(1).replaceAll("\\\\([\\\\\\]])", "$1").trim();
        }
        return null;
    }

    private String formatResultString(String rawResult) {
        if (rawResult == null) return "";
        String trimmed = rawResult.trim();
        if (trimmed.isEmpty()) return "";

        if (trimmed.matches(".*[\\u3040-\\u30ff\\u4e00-\\u9faf].*")) {
            return trimmed;
        }

        String upper = trimmed.toUpperCase();
        if ("B+R".equals(upper) || "B+RESIGN".equals(upper) || "BLACK+R".equals(upper) || "BLACK+RESIGN".equals(upper)) {
            return "黒中押し勝ち";
        }
        if ("W+R".equals(upper) || "W+RESIGN".equals(upper) || "WHITE+R".equals(upper) || "WHITE+RESIGN".equals(upper)) {
            return "白中押し勝ち";
        }
        if ("B+T".equals(upper) || "B+TIME".equals(upper)) {
            return "黒時間切れ勝ち";
        }
        if ("W+T".equals(upper) || "W+TIME".equals(upper)) {
            return "白時間切れ勝ち";
        }
        if ("B+F".equals(upper) || "B+FORFEIT".equals(upper)) {
            return "黒反則勝ち";
        }
        if ("W+F".equals(upper) || "W+FORFEIT".equals(upper)) {
            return "白反則勝ち";
        }
        if ("0".equals(upper) || "VOID".equals(upper) || "DRAW".equals(upper) || "JIGO".equals(upper)) {
            return "持碁";
        }

        Matcher bScoreMatch = Pattern.compile("^B\\+([0-9.]+)").matcher(upper);
        if (bScoreMatch.find()) {
            return "黒" + bScoreMatch.group(1) + "目勝ち";
        }

        Matcher wScoreMatch = Pattern.compile("^W\\+([0-9.]+)").matcher(upper);
        if (wScoreMatch.find()) {
            return "白" + wScoreMatch.group(1) + "目勝ち";
        }

        return trimmed;
    }

    private String extractField(JsonNode node, String... keys) {
        if (node == null) return null;
        for (String key : keys) {
            if (node.hasNonNull(key)) {
                return node.get(key).asText();
            }
        }
        if (node.isArray() && node.size() > 0) {
            for (JsonNode elem : node) {
                for (String key : keys) {
                    if (elem.hasNonNull(key)) {
                        return elem.get(key).asText();
                    }
                }
            }
        }
        return null;
    }

    private void extractDateFromFileName(String fileName, TreeSet<String> dateSet) {
        if (fileName != null && fileName.length() >= 8) {
            String prefix = fileName.substring(0, 8);
            if (prefix.matches("\\d{8}")) {
                String todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                if (prefix.compareTo(todayStr) <= 0) {
                    dateSet.add(prefix);
                }
            }
        }
    }

    public String analyze(KatagoAnalyzeRequest request) {
        // 日付の取得（リクエストの日付、またはシステム日付）
        String dateStr = (request != null && request.getDate() != null && !request.getDate().trim().isEmpty())
                ? request.getDate().trim()
                : LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // YYYYMM (サブディレクトリ名) の抽出
        String yearMonth = dateStr.length() >= 6 ? dateStr.substring(0, 6) : dateStr;
        Path targetDir = Paths.get(cacheBaseDir, yearMonth);

        logger.info("Searching analyze cache file for date {} in {}", dateStr, targetDir);

        if (!Files.exists(targetDir) || !Files.isDirectory(targetDir)) {
            // サブディレクトリが存在しない場合、直下もフォールバック確認
            targetDir = Paths.get(cacheBaseDir);
        }

        // YYYYMMDD_*.json にマッチするファイルを検索
        List<Path> matchingFiles = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(targetDir, dateStr + "_*.json")) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    matchingFiles.add(entry);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to list cache files in directory: {}", targetDir, e);
            throw new RuntimeException("キャッシュディレクトリの読み込みに失敗しました", e);
        }

        // dateStr_*.json で見つからない場合、dateStr*.json でもフォールバック検索
        if (matchingFiles.isEmpty()) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(targetDir, dateStr + "*.json")) {
                for (Path entry : stream) {
                    if (Files.isRegularFile(entry)) {
                        matchingFiles.add(entry);
                    }
                }
            } catch (IOException ignored) {
            }
        }

        if (matchingFiles.isEmpty()) {
            logger.warn("No cache file found matching {} in {}", dateStr, targetDir);
            throw new RuntimeException(new FileNotFoundException("指定日 (" + dateStr + ") のJSONファイルが見つかりません: " + targetDir));
        }

        matchingFiles.sort(Comparator.comparing(Path::getFileName));
        Path targetFile = matchingFiles.get(0);

        logger.info("Found cache file: {}", targetFile);
        try {
            return Files.readString(targetFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Failed to read cache file: {}", targetFile, e);
            throw new RuntimeException("キャッシュファイルの読み込みに失敗しました: " + targetFile, e);
        }
    }
}

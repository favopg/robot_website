package com.example.robotwebsite.service;

import com.example.robotwebsite.dto.KatagoAnalyzeRequest;
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

@Service
public class KatagoAnalyzeService {

    private static final Logger logger = LoggerFactory.getLogger(KatagoAnalyzeService.class);

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

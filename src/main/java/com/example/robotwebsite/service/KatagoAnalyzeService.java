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

@Service
public class KatagoAnalyzeService {

    private static final Logger logger = LoggerFactory.getLogger(KatagoAnalyzeService.class);

    @Value("${analyze.cache.dir:C:/analyze_cache}")
    private String cacheBaseDir;

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
            logger.warn("Cache directory does not exist: {}", targetDir);
            throw new RuntimeException(new FileNotFoundException("ディレクトリが見つかりません: " + targetDir));
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

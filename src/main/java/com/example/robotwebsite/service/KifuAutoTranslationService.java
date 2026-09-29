package com.example.robotwebsite.service;

import com.example.robotwebsite.util.KoreanTranslationUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 棋譜メタ情報（棋士名・棋戦名・対局結果等）の自動翻訳サービス
 * 1. 静的辞書/ルール変換
 * 2. インメモリキャッシュ
 * 3. 外部自動翻訳API（DeepL / Google Cloud Translation / 汎用API）
 */
@Service
public class KifuAutoTranslationService {

    private static final Logger logger = LoggerFactory.getLogger(KifuAutoTranslationService.class);

    private static final Pattern NON_JAPANESE_PATTERN = Pattern.compile("[\\uac00-\\ud7af\\u1100-\\u11ff\\u3130-\\u318f\\u4e00-\\u9fff]");

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 翻訳結果のインメモリキャッシュ (Key: 原文, Value: 翻訳結果)
    private final Map<String, String> translationCache = new ConcurrentHashMap<>();

    @Value("${translation.api.type:deepl}")
    private String apiType;

    @Value("${translation.api.key:}")
    private String apiKey;

    @Value("${translation.api.url:}")
    private String apiUrl;

    public KifuAutoTranslationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 汎用テキストを日本語に翻訳（辞書 -> キャッシュ -> 外部API）
     */
    public String translateToJapanese(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }

        // 1. 既存の辞書・ルール（KoreanTranslationUtil等）で翻訳可能か確認
        String dictResult = KoreanTranslationUtil.translate(trimmed);
        if (dictResult != null && !dictResult.equals(trimmed)) {
            return dictResult;
        }

        // 2. キャッシュに存在するか確認
        if (translationCache.containsKey(trimmed)) {
            return translationCache.get(trimmed);
        }

        // 3. APIキーが未設定、または対象テキストに翻訳の必要がない場合はそのまま返却
        if (apiKey == null || apiKey.isBlank()) {
            return trimmed;
        }

        // 4. 外部自動翻訳APIを呼び出し、キャッシュに保存
        try {
            String translated = callExternalTranslationApi(trimmed);
            if (translated != null && !translated.isBlank()) {
                translationCache.put(trimmed, translated);
                return translated;
            }
        } catch (Exception e) {
            logger.warn("外部自動翻訳APIの呼び出しに失敗しました: text={}", trimmed, e);
        }

        return trimmed;
    }

    /**
     * 対局者名を日本語に翻訳
     */
    public String translatePlayerName(String playerName) {
        if (playerName == null) return null;
        String trimmed = playerName.trim();
        if (trimmed.isEmpty()) return trimmed;

        // 1. 辞書
        String dictResult = KoreanTranslationUtil.translatePlayerName(trimmed);
        if (dictResult != null && !dictResult.equals(trimmed)) {
            return dictResult;
        }

        // 2. キャッシュ
        if (translationCache.containsKey(trimmed)) {
            return translationCache.get(trimmed);
        }

        // 3. 外部API
        if (apiKey == null || apiKey.isBlank()) {
            return trimmed;
        }

        try {
            String translated = callExternalTranslationApi(trimmed);
            if (translated != null && !translated.isBlank()) {
                translationCache.put(trimmed, translated);
                return translated;
            }
        } catch (Exception e) {
            logger.warn("対局者名の自動翻訳API呼び出しに失敗しました: playerName={}", trimmed, e);
        }

        return trimmed;
    }

    /**
     * 棋戦名を日本語に翻訳
     */
    public String translateMatchName(String matchName) {
        if (matchName == null) return null;
        String trimmed = matchName.trim();
        if (trimmed.isEmpty()) return trimmed;

        // 1. 辞書
        String dictResult = KoreanTranslationUtil.translateMatchName(trimmed);
        if (dictResult != null && !dictResult.equals(trimmed)) {
            return dictResult;
        }

        // 2. キャッシュ
        if (translationCache.containsKey(trimmed)) {
            return translationCache.get(trimmed);
        }

        // 3. 外部API
        if (apiKey == null || apiKey.isBlank()) {
            return trimmed;
        }

        try {
            String translated = callExternalTranslationApi(trimmed);
            if (translated != null && !translated.isBlank()) {
                translationCache.put(trimmed, translated);
                return translated;
            }
        } catch (Exception e) {
            logger.warn("棋戦名の自動翻訳API呼び出しに失敗しました: matchName={}", trimmed, e);
        }

        return trimmed;
    }

    /**
     * 対局結果を日本語に翻訳（対局結果は定型ルールが主）
     */
    public String translateResult(String result) {
        if (result == null) return "";
        return KoreanTranslationUtil.translateResult(result);
    }

    /**
     * キャッシュへの手動追加/クリア等（運用用）
     */
    public void putCache(String original, String translated) {
        if (original != null && translated != null) {
            translationCache.put(original.trim(), translated.trim());
        }
    }

    public void clearCache() {
        translationCache.clear();
    }

    public Map<String, String> getTranslationCache() {
        return Map.copyOf(translationCache);
    }

    /**
     * 外部自動翻訳API（DeepL / Google / 汎用）へのリクエスト
     */
    private String callExternalTranslationApi(String text) {
        if ("google".equalsIgnoreCase(apiType)) {
            return callGoogleTranslateApi(text);
        } else {
            // デフォルト: DeepL
            return callDeepLApi(text);
        }
    }

    /**
     * DeepL API呼び出し (Free / Pro)
     */
    private String callDeepLApi(String text) {
        String endpoint = apiUrl != null && !apiUrl.isBlank() ? apiUrl : "https://api-free.deepl.com/v2/translate";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "DeepL-Auth-Key " + apiKey.trim());

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("text", text);
        body.add("target_lang", "JA");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.POST, requestEntity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.has("translations") && root.get("translations").isArray() && root.get("translations").size() > 0) {
                    JsonNode first = root.get("translations").get(0);
                    if (first.hasNonNull("text")) {
                        return first.get("text").asText().trim();
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("DeepL APIリクエストでエラーが発生しました: endpoint={}, text={}", endpoint, text, e);
        }

        return null;
    }

    /**
     * Google Cloud Translation API (v2) 呼び出し
     */
    private String callGoogleTranslateApi(String text) {
        String endpoint = apiUrl != null && !apiUrl.isBlank() ? apiUrl : "https://translation.googleapis.com/language/translate/v2";
        String urlWithKey = endpoint + "?key=" + apiKey.trim();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
                "q", text,
                "target", "ja"
        );

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(urlWithKey, HttpMethod.POST, requestEntity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode data = root.path("data").path("translations");
                if (data.isArray() && data.size() > 0) {
                    JsonNode first = data.get(0);
                    if (first.hasNonNull("translatedText")) {
                        return first.get("translatedText").asText().trim();
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Google Cloud Translation APIリクエストでエラーが発生しました: text={}", text, e);
        }

        return null;
    }
}

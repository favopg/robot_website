package com.example.robotwebsite.batch;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class SiteSourceCheckTasklet implements Tasklet {

    private static final Logger logger = LoggerFactory.getLogger(SiteSourceCheckTasklet.class);
    private static final int MAX_PAGES_PER_SOURCE = 10;

    private final JdbcTemplate jdbcTemplate;

    public SiteSourceCheckTasklet(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        String sql = "SELECT id, site_name, url, type FROM site_sources WHERE enabled = TRUE";
        List<Map<String, Object>> sources = jdbcTemplate.queryForList(sql);

        if (sources.isEmpty()) {
            logger.info("No enabled site sources found.");
            return RepeatStatus.FINISHED;
        }

        List<EventRecord> allEvents = new ArrayList<>();
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();

            for (Map<String, Object> source : sources) {
                Long id = (Long) source.get("id");
                String siteName = (String) source.get("site_name");
                String startUrl = (String) source.get("url");
                String genre = (String) source.get("type");

                logger.info("Scraping site: {} starting from {}", siteName, startUrl);
                allEvents.addAll(crawlSite(page, id, startUrl, genre));
            }
            browser.close();
        }

        saveEvents(allEvents);

        return RepeatStatus.FINISHED;
    }

    private List<EventRecord> crawlSite(Page page, Long siteSourceId, String startUrl, String genre) {
        List<EventRecord> scrapedEvents = new ArrayList<>();
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        queue.add(startUrl);

        URI startUri;
        try {
            startUri = new URI(startUrl);
        } catch (Exception e) {
            logger.error("Invalid start URL: " + startUrl, e);
            return scrapedEvents;
        }

        int pagesCrawled = 0;
        while (!queue.isEmpty() && pagesCrawled < MAX_PAGES_PER_SOURCE) {
            String currentUrl = queue.poll();
            if (visited.contains(currentUrl)) {
                continue;
            }

            try {
                logger.info("Navigating to: {}", currentUrl);
                page.navigate(currentUrl);
                page.waitForSelector("body");
                visited.add(currentUrl);
                pagesCrawled++;

                // イベント抽出
                List<EventRecord> events = scrapeEventsFromPage(page, siteSourceId, genre);
                logger.info("Scraped {} events from {}", events.size(), currentUrl);
                scrapedEvents.addAll(events);

                // リンク収集
                List<ElementHandle> links = page.querySelectorAll("a[href]");
                for (ElementHandle link : links) {
                    String href = link.getAttribute("href");
                    if (href == null || href.isEmpty() || href.startsWith("#") || href.startsWith("javascript:")) {
                        continue;
                    }

                    try {
                        URI resolvedUri = startUri.resolve(href).normalize();
                        String resolvedUrl = resolvedUri.toString();

                        // 同一ホスト、かつ同一 /event/ 配下かチェック
                        if (resolvedUri.getHost() != null && resolvedUri.getHost().equals(startUri.getHost())
                                && resolvedUri.getPath() != null && resolvedUri.getPath().startsWith("/event/")) {
                            
                            // 明らかにイベントと関係なさそうな拡張子を除外
                            if (resolvedUrl.toLowerCase().endsWith(".pdf") || 
                                resolvedUrl.toLowerCase().endsWith(".jpg") || 
                                resolvedUrl.toLowerCase().endsWith(".png") ||
                                resolvedUrl.toLowerCase().endsWith(".zip")) {
                                continue;
                            }

                            if (!visited.contains(resolvedUrl) && !queue.contains(resolvedUrl)) {
                                queue.add(resolvedUrl);
                            }
                        }
                    } catch (Exception e) {
                        // Ignore invalid URIs
                    }
                }

            } catch (Exception e) {
                logger.error("Failed to crawl page: " + currentUrl, e);
            }
        }
        return scrapedEvents;
    }

    private List<EventRecord> scrapeEventsFromPage(Page page, Long siteSourceId, String genre) {
        List<EventRecord> events = new ArrayList<>();
        LocalDate today = LocalDate.now();
        URI baseUri;
        try {
            baseUri = new URI(page.url());
        } catch (Exception e) {
            logger.error("Invalid page URL: " + page.url(), e);
            return events;
        }

        // テーブル形式のイベント情報を取得
        List<ElementHandle> rows = page.querySelectorAll("table tr");
        for (ElementHandle row : rows) {
            List<ElementHandle> cells = row.querySelectorAll("td");
            if (cells.size() >= 2) {
                ElementHandle titleLink = cells.get(1).querySelector("a");
                if (titleLink != null) {
                    String title = titleLink.innerText().trim();
                    String href = titleLink.getAttribute("href");
                    String eventUrl = page.url();
                    if (href != null) {
                        try {
                            eventUrl = baseUri.resolve(href).normalize().toString();
                        } catch (Exception e) {
                            // ignore
                        }
                    }

                    String infoText = row.innerText();
                    LocalDate eventDate = extractDate(infoText);
                    
                    // システム年月以降のイベントのみ取得
                    if (eventDate != null && eventDate.isBefore(today)) {
                        logger.info("Skipping past event: {} (Date: {})", title, eventDate);
                        continue;
                    }

                    boolean beginner = infoText.contains("初心者") || infoText.contains("入門") || infoText.contains("はじめて");
                    boolean kyu = infoText.contains("級位者") || infoText.contains("級") || infoText.contains("10級");
                    boolean dan = infoText.contains("有段者") || infoText.contains("段") || infoText.contains("五段");

                    events.add(new EventRecord(
                            siteSourceId,
                            title,
                            eventDate,
                            null,
                            eventUrl,
                            infoText.length() > 1000 ? infoText.substring(0, 1000) : infoText,
                            genre,
                            beginner,
                            kyu,
                            dan
                    ));
                }
            }
        }

        // 既存のセレクタも予備として残す
        if (events.isEmpty()) {
            List<ElementHandle> eventElements = page.querySelectorAll("ul.event_list li, .event_item, article, section.event");
            for (ElementHandle el : eventElements) {
                try {
                    ElementHandle titleEl = el.querySelector("dt, h2, h3, .title");
                    if (titleEl == null) continue;
                    String title = titleEl.innerText().trim();
                    if (title.isEmpty()) continue;

                    ElementHandle linkEl = el.querySelector("a");
                    String href = linkEl != null ? linkEl.getAttribute("href") : null;
                    String eventUrl = page.url();
                    if (href != null) {
                        try {
                            eventUrl = baseUri.resolve(href).normalize().toString();
                        } catch (Exception e) {
                            // ignore
                        }
                    }

                    String infoText = el.innerText();
                    LocalDate eventDate = extractDate(infoText);

                    // システム年月以降のイベントのみ取得
                    if (eventDate != null && eventDate.isBefore(today)) {
                        logger.info("Skipping past event: {} (Date: {})", title, eventDate);
                        continue;
                    }

                    boolean beginner = infoText.contains("初心者") || infoText.contains("入門") || infoText.contains("はじめて");
                    boolean kyu = infoText.contains("級位者") || infoText.contains("級") || infoText.contains("10級");
                    boolean dan = infoText.contains("有段者") || infoText.contains("段") || infoText.contains("五段");

                    events.add(new EventRecord(
                            siteSourceId,
                            title,
                            eventDate,
                            null,
                            eventUrl,
                            infoText.length() > 1000 ? infoText.substring(0, 1000) : infoText,
                            genre,
                            beginner,
                            kyu,
                            dan
                    ));
                } catch (Exception e) {
                    // Ignore
                }
            }
        }

        return events;
    }

    private LocalDate extractDate(String text) {
        if (text == null) return null;
        // 2026/06/23 or 2026-06-23 or 2026年6月23日 などのパターンを探す
        Pattern pattern = Pattern.compile("(\\d{4})[/-年](\\d{1,2})[/-月](\\d{1,2})日?");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                int year = Integer.parseInt(matcher.group(1));
                int month = Integer.parseInt(matcher.group(2));
                int day = Integer.parseInt(matcher.group(3));
                return LocalDate.of(year, month, day);
            } catch (Exception e) {
                // Ignore
            }
        }
        return null;
    }

    private void saveEvents(List<EventRecord> events) {
        for (EventRecord event : events) {
            try {
                // 重複登録を避けるため MERGE 相当の処理を行う（URLがUNIQUE制約付き）
                String sql = "INSERT INTO events (site_source_id, title, event_date, location, url, description, genre, target_beginner, target_kyu_player, target_dan_player) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                           "ON DUPLICATE KEY UPDATE title = VALUES(title)"; 
                // H2の場合は MERGE INTO を使うのが一般的
                String h2Sql = "MERGE INTO events (site_source_id, title, event_date, location, url, description, genre, target_beginner, target_kyu_player, target_dan_player) " +
                             "KEY(url) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                
                jdbcTemplate.update(h2Sql,
                    event.siteSourceId(),
                    event.title(),
                    event.eventDate(),
                    event.location(),
                    event.url(),
                    event.description(),
                    event.genre(),
                    event.targetBeginner(),
                    event.targetKyuPlayer(),
                    event.targetDanPlayer()
                );
            } catch (Exception e) {
                logger.error("Failed to save event: " + event.title(), e);
            }
        }
    }
}

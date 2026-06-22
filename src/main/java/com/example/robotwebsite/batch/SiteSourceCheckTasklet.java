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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SiteSourceCheckTasklet implements Tasklet {

    private static final Logger logger = LoggerFactory.getLogger(SiteSourceCheckTasklet.class);

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

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();

            for (Map<String, Object> source : sources) {
                Long id = (Long) source.get("id");
                String siteName = (String) source.get("site_name");
                String url = (String) source.get("url");
                String genre = (String) source.get("type");

                logger.info("Scraping site: {} ({})", siteName, url);

                try {
                    page.navigate(url);
                    page.waitForSelector("body");
                    logger.info("Navigated to {}", url);
                    List<EventRecord> events = scrapeEvents(page, id, genre);
                    logger.info("Scraped {} events from {}", events.size(), siteName);
                    saveEvents(events);
                } catch (Exception e) {
                    logger.error("Failed to scrape site: " + siteName, e);
                }
            }
            browser.close();
        }

        return RepeatStatus.FINISHED;
    }

    private List<EventRecord> scrapeEvents(Page page, Long siteSourceId, String genre) {
        List<EventRecord> events = new ArrayList<>();

        // テーブル形式のイベント情報を取得
        List<ElementHandle> rows = page.querySelectorAll("table tr");
        for (ElementHandle row : rows) {
            List<ElementHandle> cells = row.querySelectorAll("td");
            if (cells.size() >= 2) {
                ElementHandle titleLink = cells.get(1).querySelector("a");
                if (titleLink != null) {
                    String title = titleLink.innerText().trim();
                    String eventUrl = titleLink.getAttribute("href");
                    if (eventUrl != null && !eventUrl.startsWith("http")) {
                        if (eventUrl.startsWith("/")) {
                            eventUrl = "https://www.nihonkiin.or.jp" + eventUrl;
                        } else {
                            eventUrl = page.url() + eventUrl;
                        }
                    }

                    String infoText = row.innerText();
                    boolean beginner = infoText.contains("初心者") || infoText.contains("入門") || infoText.contains("はじめて");
                    boolean kyu = infoText.contains("級位者") || infoText.contains("級") || infoText.contains("10級");
                    boolean dan = infoText.contains("有段者") || infoText.contains("段") || infoText.contains("五段");

                    events.add(new EventRecord(
                            siteSourceId,
                            title,
                            null,
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
                    String eventUrl = linkEl != null ? linkEl.getAttribute("href") : page.url();
                    if (eventUrl != null && !eventUrl.startsWith("http")) {
                        if (eventUrl.startsWith("/")) {
                            eventUrl = "https://www.nihonkiin.or.jp" + eventUrl;
                        } else {
                            eventUrl = page.url() + eventUrl;
                        }
                    }

                    String infoText = el.innerText();
                    boolean beginner = infoText.contains("初心者") || infoText.contains("入門") || infoText.contains("はじめて");
                    boolean kyu = infoText.contains("級位者") || infoText.contains("級") || infoText.contains("10級");
                    boolean dan = infoText.contains("有段者") || infoText.contains("段") || infoText.contains("五段");

                    events.add(new EventRecord(
                            siteSourceId,
                            title,
                            null,
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

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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        try {
            logger.info("Navigating to: {}", startUrl);
            page.navigate(startUrl);
            page.waitForSelector("body");

            // イベント抽出
            List<EventRecord> events = scrapeEventsFromPage(page, siteSourceId, genre);
            logger.info("Scraped {} events from {}", events.size(), startUrl);
            scrapedEvents.addAll(events);

        } catch (Exception e) {
            logger.error("Failed to scrape page: " + startUrl, e);
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
                    if (title.length() > 500) title = title.substring(0, 500);

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
                    String description = infoText.length() > 1000 ? infoText.substring(0, 1000) : infoText;

                    events.add(new EventRecord(
                            siteSourceId,
                            title,
                            eventDate,
                            null,
                            eventUrl,
                            description,
                            genre
                    ));
                }
            }
        }

        // 既存のセレクタも予備として残す
        if (events.isEmpty()) {
            List<ElementHandle> eventElements = page.querySelectorAll("ul.event_list li, .event_list li, .news_list li, .news_list_item, .event_item, article, section.event, li");
            for (ElementHandle el : eventElements) {
                try {
                    // 日本棋院のニュースリストなどの特定の構造をチェック
                    ElementHandle linkEl = el.querySelector("a");
                    if (linkEl == null) continue;

                    String title = linkEl.innerText().trim();
                    if (title.length() > 500) title = title.substring(0, 500);
                    if (title.isEmpty()) continue;
                    
                    // 特定のキーワードが含まれているか、または親が特定のクラスを持っている場合のみ対象とする
                    // 汎用的な li を拾っているので、何らかの絞り込みが必要
                    String className = (String) el.getProperty("className").jsonValue();
                    String parentClassName = "";
                    ElementHandle parent = page.evaluateHandle("el => el.parentElement", el).asElement();
                    if (parent != null) {
                        parentClassName = (String) parent.getProperty("className").jsonValue();
                    }
                    
                    boolean isEventElement = className.contains("event") || className.contains("news") ||
                                            parentClassName.contains("event") || parentClassName.contains("news");
                    
                    // 日本棋院の特定の構造（日付 span + タイトル a）
                    ElementHandle dateEl = el.querySelector(".date, .time, .event-date");
                    if (!isEventElement && dateEl == null) {
                        continue;
                    }

                    String href = linkEl.getAttribute("href");
                    String eventUrl = page.url();
                    if (href != null) {
                        try {
                            eventUrl = baseUri.resolve(href).normalize().toString();
                        } catch (Exception e) {
                            // ignore
                        }
                    }

                    String infoText = el.innerText();
                    LocalDate eventDate = null;
                    if (dateEl != null) {
                        String dateStr = dateEl.innerText().trim();
                        eventDate = extractDate(dateStr);
                    }
                    if (eventDate == null) {
                        eventDate = extractDate(infoText);
                    }

                    // 日本棋院などのニュースの場合、日付が取れないものはイベントではない可能性が高い
                    if (eventDate == null && !isEventElement) {
                        continue;
                    }

                    String description = infoText.length() > 1000 ? infoText.substring(0, 1000) : infoText;

                    events.add(new EventRecord(
                            siteSourceId,
                            title,
                            eventDate,
                            null,
                            eventUrl,
                            description,
                            genre
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
                String sql = "INSERT INTO events (site_source_id, title, event_date, location, url, description, genre) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                           "ON DUPLICATE KEY UPDATE title = VALUES(title)"; 
                // H2の場合は MERGE INTO を使うのが一般的
                String h2Sql = "MERGE INTO events (site_source_id, title, event_date, location, url, description, genre) " +
                             "KEY(url) VALUES (?, ?, ?, ?, ?, ?, ?)";
                
                jdbcTemplate.update(h2Sql,
                    event.siteSourceId(),
                    event.title(),
                    event.eventDate(),
                    event.location(),
                    event.url(),
                    event.description(),
                    event.genre()
                );
            } catch (Exception e) {
                logger.error("Failed to save event: " + event.title(), e);
            }
        }
    }
}

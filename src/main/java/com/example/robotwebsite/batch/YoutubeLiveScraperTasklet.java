package com.example.robotwebsite.batch;

import com.example.robotwebsite.entity.YoutubeLive;
import com.example.robotwebsite.repository.YoutubeLiveRepository;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
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
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class YoutubeLiveScraperTasklet implements Tasklet {

    private static final Logger logger = LoggerFactory.getLogger(YoutubeLiveScraperTasklet.class);
    private static final String YOUTUBE_STREAMS_URL = "https://www.youtube.com/@nihonkiin_ch/streams";
    
    private final YoutubeLiveRepository youtubeLiveRepository;

    public YoutubeLiveScraperTasklet(YoutubeLiveRepository youtubeLiveRepository) {
        this.youtubeLiveRepository = youtubeLiveRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.info("Starting YouTube Live scraping for Nihonkiin...");

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            // Language must be set to Japanese to ensure "予定" (Upcoming) text is present
            BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale("ja-JP"));
            Page page = context.newPage();
            
            logger.info("Navigating to: {}", YOUTUBE_STREAMS_URL);
            page.navigate(YOUTUBE_STREAMS_URL);
            
            // Wait for items to load
            page.waitForSelector("ytd-rich-item-renderer");
            
            // YouTube uses lazy loading, but upcoming streams should be at the top.
            // We might need to wait a bit for metadata to load.
            page.waitForTimeout(2000);

            List<ElementHandle> items = page.querySelectorAll("ytd-rich-item-renderer");
            logger.info("Found {} items on YouTube streams page", items.size());

            int count = 0;
            for (ElementHandle item : items) {
                try {
                    // Check if it's an "Upcoming" stream
                    // Upcoming streams usually have "scheduled" text in metadata
                    ElementHandle metadataLine = item.querySelector("#metadata-line");
                    if (metadataLine == null) continue;
                    
                    String metadataText = metadataLine.innerText();
                    // Also checking for "Scheduled" in case locale setting is not fully respected or for future flexibility
                    if (metadataText == null || (!metadataText.contains("予定") && !metadataText.contains("Scheduled"))) {
                        // Skip if not scheduled/upcoming
                        continue;
                    }

                    ElementHandle titleEl = item.querySelector("#video-title");
                    if (titleEl == null) continue;
                    
                    String title = titleEl.innerText().trim();
                    String href = titleEl.getAttribute("href");
                    if (href == null) continue;
                    
                    String videoUrl = "https://www.youtube.com" + (href.contains("?") ? href.substring(0, href.indexOf("?")) : href);
                    
                    // Extract scheduling text (e.g., "2026/07/25 10:00 に公開予定")
                    String scheduledText = metadataText.trim();
                    
                    saveOrUpdateYoutubeLive(title, videoUrl, scheduledText);
                    count++;
                } catch (Exception e) {
                    logger.error("Error parsing YouTube item", e);
                }
            }
            
            logger.info("Finished YouTube Live scraping. Registered/Updated {} upcoming streams.", count);
            browser.close();
        } catch (Exception e) {
            logger.error("Failed to scrape YouTube lives", e);
            throw e;
        }

        return RepeatStatus.FINISHED;
    }

    private void saveOrUpdateYoutubeLive(String title, String url, String scheduledText) {
        Optional<YoutubeLive> existing = youtubeLiveRepository.findByLiveUrl(url);
        YoutubeLive youtubeLive = existing.orElse(new YoutubeLive());
        
        youtubeLive.setTitle(title);
        youtubeLive.setLiveUrl(url);
        youtubeLive.setScheduledStartText(scheduledText);
        // scheduledStartTime parsing could be added here if needed, but text might be enough for now as requested
        
        youtubeLiveRepository.save(youtubeLive);
    }
}
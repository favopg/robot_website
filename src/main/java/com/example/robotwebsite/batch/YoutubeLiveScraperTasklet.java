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
            page.waitForSelector("ytd-rich-grid-row, ytd-rich-item-renderer");
            
            // YouTube uses lazy loading, but upcoming streams should be at the top.
            page.waitForTimeout(5000);
            
            // Scroll down a bit to trigger loading
            page.mouse().wheel(0, 1000);
            page.waitForTimeout(3000);

            // Use more specific selectors for titles and metadata
            List<ElementHandle> items = page.querySelectorAll("ytd-rich-item-renderer, ytd-video-renderer");
            logger.info("Found {} items on YouTube streams page", items.size());

            int count = 0;
            for (ElementHandle item : items) {
                try {
                    String itemText = item.innerText().replace("\n", " ");
                    logger.info("Raw item text: {}", itemText);
                    
                    // Title and URL
                    ElementHandle titleLinkEl = item.querySelector("#video-title-link");
                    if (titleLinkEl == null) {
                        titleLinkEl = item.querySelector("a#video-title-link");
                    }
                    if (titleLinkEl == null) {
                        titleLinkEl = item.querySelector("#video-title");
                    }
                    
                    // Fallback for finding the link: look for any <a> tag containing "/watch" in href
                    if (titleLinkEl == null) {
                        List<ElementHandle> links = item.querySelectorAll("a");
                        for (ElementHandle link : links) {
                            String linkHref = link.getAttribute("href");
                            if (linkHref != null && linkHref.contains("/watch")) {
                                titleLinkEl = link;
                                break;
                            }
                        }
                    }
                    
                    String title = "";
                    String href = "";
                    String ariaLabel = "";

                    if (titleLinkEl != null) {
                        title = titleLinkEl.innerText().trim();
                        ariaLabel = titleLinkEl.getAttribute("aria-label");
                        if (ariaLabel == null) ariaLabel = "";
                        
                        if (title.isEmpty() && !ariaLabel.isEmpty()) {
                            title = ariaLabel;
                        }

                        href = titleLinkEl.getAttribute("href");
                    }

                    // Final fallback for title if still empty: use itemText but try to clean it
                    if (title.isEmpty()) {
                        title = itemText;
                        if (title.contains("公開予定")) {
                            title = title.substring(0, title.lastIndexOf("公開予定")).trim();
                        }
                    }
                    
                    if (href == null || href.isEmpty()) {
                        // If we still don't have a href, we can't save it as a live event
                        logger.warn("Could not find link (href) for an item. Item text: {}", itemText);
                        continue;
                    }

                    // Metadata (scheduled time)
                    String metadataText = "";
                    ElementHandle metadataLine = item.querySelector("#metadata-line");
                    if (metadataLine != null) {
                        metadataText = metadataLine.innerText();
                    } else {
                        // Try fallback for metadata
                        metadataText = itemText; // Use the raw text we already got
                    }
                    
                    String checkText = (metadataText + " " + ariaLabel + " " + title).toLowerCase();
                    logger.info("Checking item: Title: {}, Aria-label: {}, checkText: {}", 
                                title, 
                                ariaLabel,
                                checkText);

                    // Keyword check
                    boolean isUpcoming = false;
                    if (checkText.contains("予定") || checkText.contains("scheduled") || 
                        checkText.contains("待機中") || checkText.contains("upcoming") ||
                        checkText.matches(".*\\d{4}/\\d{2}/\\d{2}.*")) {
                        isUpcoming = true;
                    }

                    if (!isUpcoming) {
                        logger.info("Item skipped (not upcoming)");
                        continue;
                    }
                    
                    String videoUrl = "https://www.youtube.com" + href;
                    String scheduledText = metadataText.trim();
                    if (scheduledText.isEmpty() && !ariaLabel.isEmpty()) {
                        scheduledText = ariaLabel;
                    }
                    
                    // Truncate strings to prevent database errors (assuming max 1000 after DB update)
                    if (title.length() > 1000) title = title.substring(0, 997) + "...";
                    if (scheduledText.length() > 1000) scheduledText = scheduledText.substring(0, 997) + "...";
                    
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
package com.example.robotwebsite;

import com.example.robotwebsite.batch.YoutubeLiveScraperTasklet;
import com.example.robotwebsite.repository.YoutubeLiveRepository;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class YoutubeLiveScraperTest {

    @Autowired
    private YoutubeLiveScraperTasklet tasklet;

    @Autowired
    private YoutubeLiveRepository youtubeLiveRepository;

    @Test
    public void testScrape() throws Exception {
        // Clear existing data
        youtubeLiveRepository.deleteAll();

        // Run tasklet
        RepeatStatus status = tasklet.execute(null, null);
        
        assertEquals(RepeatStatus.FINISHED, status);
        
        long count = youtubeLiveRepository.count();
        System.out.println("[DEBUG_LOG] Scraped " + count + " YouTube live streams.");
        
        youtubeLiveRepository.findAll().forEach(live -> {
            System.out.println("[DEBUG_LOG] Title: " + live.getTitle());
            System.out.println("[DEBUG_LOG] URL: " + live.getLiveUrl());
            System.out.println("[DEBUG_LOG] Scheduled: " + live.getScheduledStartText());
        });
    }
}

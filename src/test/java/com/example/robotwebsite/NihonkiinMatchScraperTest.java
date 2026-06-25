package com.example.robotwebsite;

import com.example.robotwebsite.batch.NihonkiinMatchScraperTasklet;
import com.example.robotwebsite.entity.Match;
import com.example.robotwebsite.repository.MatchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class NihonkiinMatchScraperTest {

    @Autowired
    private NihonkiinMatchScraperTasklet scraperTasklet;

    @Autowired
    private MatchRepository matchRepository;

    @Test
    public void testScraping() throws Exception {
        // Run scraper
        RepeatStatus status = scraperTasklet.execute(null, null);
        assertEquals(RepeatStatus.FINISHED, status);

        // Verify data was saved
        List<Match> matches = matchRepository.findAll();
        System.out.println("[DEBUG_LOG] Scraped matches count: " + matches.size());
        assertFalse(matches.isEmpty(), "Matches should have been scraped and saved");

        System.out.println("[DEBUG_LOG] Scraped matches count: " + matches.size());
        for (int i = 0; i < Math.min(5, matches.size()); i++) {
            Match m = matches.get(i);
            System.out.println("[DEBUG_LOG] Match: " + m.getMatchDate() + " " + m.getMatchName() + " " + m.getPlayer1Name() + " vs " + m.getPlayer2Name() + " result: " + m.getResult());
        }
        
        // Check if there are both results and schedule
        long resultsCount = matches.stream().filter(m -> m.getResult() != null).count();
        long scheduleCount = matches.stream().filter(m -> m.getResult() == null).count();
        
        System.out.println("[DEBUG_LOG] Results count: " + resultsCount);
        System.out.println("[DEBUG_LOG] Schedule count: " + scheduleCount);
        
        assertTrue(resultsCount > 0, "Should have scraped some results");
        assertTrue(scheduleCount > 0, "Should have scraped some scheduled matches");
    }
}

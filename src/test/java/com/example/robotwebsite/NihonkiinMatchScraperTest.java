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
    public void testScrapingTwice() throws Exception {
        // Run scraper first time
        RepeatStatus status1 = scraperTasklet.execute(null, null);
        assertEquals(RepeatStatus.FINISHED, status1);
        
        long count1 = matchRepository.count();
        assertTrue(count1 > 0);

        // Run scraper second time - this should not cause UnexpectedRollbackException
        RepeatStatus status2 = scraperTasklet.execute(null, null);
        assertEquals(RepeatStatus.FINISHED, status2);
        
        long count2 = matchRepository.count();
        assertEquals(count1, count2, "Count should be same after second run (if no new data on website)");
    }
}

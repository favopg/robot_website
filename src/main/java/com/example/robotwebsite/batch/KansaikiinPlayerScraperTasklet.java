package com.example.robotwebsite.batch;

import com.example.robotwebsite.service.SystemStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
public class KansaikiinPlayerScraperTasklet implements Tasklet {

    private static final Logger logger = LoggerFactory.getLogger(KansaikiinPlayerScraperTasklet.class);
    private final KansaikiinPlayerScraper scraper;
    private final SystemStatusService systemStatusService;

    public KansaikiinPlayerScraperTasklet(KansaikiinPlayerScraper scraper, SystemStatusService systemStatusService) {
        this.scraper = scraper;
        this.systemStatusService = systemStatusService;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.info("Starting KansaikiinPlayerScraperTasklet...");
        try {
            systemStatusService.setUpdating(true);
            scraper.scrapeAllPlayers();
        } finally {
            systemStatusService.setUpdating(false);
        }
        logger.info("KansaikiinPlayerScraperTasklet finished.");
        return RepeatStatus.FINISHED;
    }
}

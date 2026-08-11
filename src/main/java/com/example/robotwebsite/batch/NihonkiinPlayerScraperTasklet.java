package com.example.robotwebsite.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import com.example.robotwebsite.service.SystemStatusService;

@Component
public class NihonkiinPlayerScraperTasklet implements Tasklet {

    private static final Logger logger = LoggerFactory.getLogger(NihonkiinPlayerScraperTasklet.class);
    private final NihonkiinPlayerScraper scraper;
    private final SystemStatusService systemStatusService;

    public NihonkiinPlayerScraperTasklet(NihonkiinPlayerScraper scraper, SystemStatusService systemStatusService) {
        this.scraper = scraper;
        this.systemStatusService = systemStatusService;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        String stepName = chunkContext.getStepContext().getStepName();
        logger.info("Starting NihonkiinPlayerScraperTasklet for step: " + stepName);
        try {
            systemStatusService.setUpdating(true);
            if ("playerBasicScrapeStep".equals(stepName)) {
                scraper.scrapeAllPlayersBasicInfo();
            } else if ("playerStatsScrapeStep".equals(stepName)) {
                scraper.scrapeAllPlayersStats();
            } else {
                scraper.scrapeAllPlayers();
            }
        } finally {
            systemStatusService.setUpdating(false);
        }
        logger.info("NihonkiinPlayerScraperTasklet finished step: " + stepName);
        return RepeatStatus.FINISHED;
    }
}

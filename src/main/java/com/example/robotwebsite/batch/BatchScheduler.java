package com.example.robotwebsite.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;

import java.util.Date;

@Component
public class BatchScheduler implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(BatchScheduler.class);

    private final JobLauncher jobLauncher;
    private final Job siteSourceCheckJob;
    private final Job nihonkiinMatchJob;
    private final Job youtubeLiveScrapeJob;
    private final Job normalizationJob;

    public BatchScheduler(JobLauncher jobLauncher, Job siteSourceCheckJob, Job nihonkiinMatchJob, Job youtubeLiveScrapeJob, Job normalizationJob) {
        this.jobLauncher = jobLauncher;
        this.siteSourceCheckJob = siteSourceCheckJob;
        this.nihonkiinMatchJob = nihonkiinMatchJob;
        this.youtubeLiveScrapeJob = youtubeLiveScrapeJob;
        this.normalizationJob = normalizationJob;
    }

    @Override
    public void run(String... args) {
        // 移行ジョブを実行
        runNormalizationJob();
        // runJob();
        runNihonkiinMatchJob();
        // runYoutubeLiveScrapeJob();
    }

    // 毎週金曜日23時59分に実行
    // @org.springframework.scheduling.annotation.Scheduled(cron = "0 59 23 * * FRI")
    public void runJob() {
        try {
            logger.info("Starting siteSourceCheckJob at " + new Date());
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(siteSourceCheckJob, params);
        } catch (Exception e) {
            logger.error("Error executing siteSourceCheckJob", e);
        }
    }

    // 毎週金曜日23時59分に実行
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 59 23 * * FRI")
    public void runNihonkiinMatchJob() {
        try {
            logger.info("Starting nihonkiinMatchJob at " + new Date());
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(nihonkiinMatchJob, params);
        } catch (Exception e) {
            logger.error("Error executing nihonkiinMatchJob", e);
        }
    }
    // 1時間に1回実行 (3,600,000ミリ秒)
    // @org.springframework.scheduling.annotation.Scheduled(fixedRate = 3600000)
    public void runYoutubeLiveScrapeJob() {
        try {
            logger.info("Starting youtubeLiveScrapeJob at " + new Date());
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(youtubeLiveScrapeJob, params);
        } catch (Exception e) {
            logger.error("Error executing youtubeLiveScrapeJob", e);
        }
    }

    public void runNormalizationJob() {
        try {
            logger.info("Starting normalizationJob at " + new Date());
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(normalizationJob, params);
        } catch (Exception e) {
            logger.error("Error executing normalizationJob", e);
        }
    }
}

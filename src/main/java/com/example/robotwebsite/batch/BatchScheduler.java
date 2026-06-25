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

    public BatchScheduler(JobLauncher jobLauncher, Job siteSourceCheckJob, Job nihonkiinMatchJob) {
        this.jobLauncher = jobLauncher;
        this.siteSourceCheckJob = siteSourceCheckJob;
        this.nihonkiinMatchJob = nihonkiinMatchJob;
    }

    @Override
    public void run(String... args) {
        runJob();
        runNihonkiinMatchJob();
    }

    // 30分に1回実行 (1,800,000ミリ秒)
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 1800000)
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

    // 5分間隔で実行 (300,000ミリ秒)
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 300000)
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
}

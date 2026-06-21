package com.example.robotwebsite.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@EnableScheduling
public class BatchScheduler {

    private static final Logger logger = LoggerFactory.getLogger(BatchScheduler.class);

    private final JobLauncher jobLauncher;
    private final Job siteSourceCheckJob;

    public BatchScheduler(JobLauncher jobLauncher, Job siteSourceCheckJob) {
        this.jobLauncher = jobLauncher;
        this.siteSourceCheckJob = siteSourceCheckJob;
    }

    // 5分に1回実行 (300,000ミリ秒)
    @Scheduled(fixedRate = 300000)
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
}

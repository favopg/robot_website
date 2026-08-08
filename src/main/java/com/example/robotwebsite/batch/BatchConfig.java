package com.example.robotwebsite.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

    @Bean
    public Job siteSourceCheckJob(JobRepository jobRepository, Step siteSourceCheckStep) {
        return new JobBuilder("siteSourceCheckJob", jobRepository)
                .start(siteSourceCheckStep)
                .build();
    }

    @Bean
    public Step siteSourceCheckStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, SiteSourceCheckTasklet tasklet) {
        return new StepBuilder("siteSourceCheckStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    public Job nihonkiinMatchJob(JobRepository jobRepository, Step nihonkiinMatchStep) {
        return new JobBuilder("nihonkiinMatchJob", jobRepository)
                .start(nihonkiinMatchStep)
                .build();
    }

    @Bean
    public Step nihonkiinMatchStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, NihonkiinMatchScraperTasklet tasklet) {
        return new StepBuilder("nihonkiinMatchStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }
    @Bean
    public Job youtubeLiveScrapeJob(JobRepository jobRepository, Step youtubeLiveScrapeStep) {
        return new JobBuilder("youtubeLiveScrapeJob", jobRepository)
                .start(youtubeLiveScrapeStep)
                .build();
    }

    @Bean
    public Step youtubeLiveScrapeStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, YoutubeLiveScraperTasklet tasklet) {
        return new StepBuilder("youtubeLiveScrapeStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    public Job normalizationJob(JobRepository jobRepository, Step normalizationStep) {
        return new JobBuilder("normalizationJob", jobRepository)
                .start(normalizationStep)
                .build();
    }

    @Bean
    public Step normalizationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, PlayerNormalizationTasklet tasklet) {
        return new StepBuilder("normalizationStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }
}

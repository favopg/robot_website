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
}

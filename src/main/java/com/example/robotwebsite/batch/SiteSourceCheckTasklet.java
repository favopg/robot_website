package com.example.robotwebsite.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SiteSourceCheckTasklet implements Tasklet {

    private static final Logger logger = LoggerFactory.getLogger(SiteSourceCheckTasklet.class);

    private final JdbcTemplate jdbcTemplate;

    public SiteSourceCheckTasklet(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        String sql = "SELECT site_name FROM site_sources";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

        if (!results.isEmpty()) {
            logger.info("Site source check found {} sites.", results.size());
            for (Map<String, Object> row : results) {
                String siteName = (String) row.get("site_name");
                System.out.println("Site Name: " + siteName);
            }
        } else {
            logger.info("No site sources found.");
        }

        return RepeatStatus.FINISHED;
    }
}

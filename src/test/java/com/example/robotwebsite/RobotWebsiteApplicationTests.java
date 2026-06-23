package com.example.robotwebsite;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ActiveProfiles("test")
class RobotWebsiteApplicationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void testBatchExecutionAndEventRegistration() {
        // 起動時にBatchSchedulerが走っているはずなので、eventsテーブルを確認する
        List<Map<String, Object>> events = jdbcTemplate.queryForList("SELECT * FROM events");
        System.out.println("[DEBUG_LOG] Fetched events count: " + events.size());
        
        for (Map<String, Object> event : events) {
            System.out.println("[DEBUG_LOG] Event: " + event.get("title") + " (Date: " + event.get("event_date") + ")");
        }
    }

}

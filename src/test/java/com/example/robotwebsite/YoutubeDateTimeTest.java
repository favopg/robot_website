package com.example.robotwebsite;

import com.example.robotwebsite.entity.YoutubeLive;
import com.example.robotwebsite.repository.YoutubeLiveRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class YoutubeDateTimeTest {

    @Autowired
    private YoutubeLiveRepository youtubeLiveRepository;

    @Test
    public void testDateTimeParsingLogic() {
        String input = "2026/08/03 10:00 に公開予定";
        Pattern pattern = Pattern.compile("(\\d{4}/\\d{2}/\\d{2})\\s+(\\d{2}:\\d{2})");
        Matcher matcher = pattern.matcher(input);
        
        assertTrue(matcher.find());
        String datePart = matcher.group(1);
        String timePart = matcher.group(2);
        
        LocalDateTime dt = LocalDateTime.parse(datePart + " " + timePart, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
        assertEquals(2026, dt.getYear());
        assertEquals(8, dt.getMonthValue());
        assertEquals(3, dt.getDayOfMonth());
        assertEquals(10, dt.getHour());
        
        LocalDateTime plus17Hours = dt.plusHours(17);
        assertEquals(2026, plus17Hours.getYear());
        assertEquals(8, plus17Hours.getMonthValue());
        assertEquals(4, plus17Hours.getDayOfMonth());
        assertEquals(3, plus17Hours.getHour());
    }
}

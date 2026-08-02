package com.example.robotwebsite;

import com.example.robotwebsite.entity.Match;
import com.example.robotwebsite.repository.MatchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TopWinnerDisplayTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MatchRepository matchRepository;

    @Test
    public void testTopWinnerDisplayAreaExists() throws Exception {
        // 2026年7月のデータを投入
        Match m1 = createMatch("2026-07-01", "一力遼", "芝野虎丸", "一力遼");
        Match m2 = createMatch("2026-07-05", "一力遼", "井山裕太", "一力遼");
        Match m3 = createMatch("2026-07-10", "芝野虎丸", "井山裕太", "芝野虎丸");
        
        matchRepository.save(m1);
        matchRepository.save(m2);
        matchRepository.save(m3);

        mockMvc.perform(get("/match-results").param("year", "2026").param("month", "7"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"topWinnerArea\"")))
                .andExpect(content().string(containsString("id=\"topWinnersList\"")));
    }

    private Match createMatch(String date, String p1, String p2, String winner) {
        Match m = new Match();
        m.setMatchDate(LocalDate.parse(date));
        m.setMatchName("Test Tournament");
        m.setPlayer1Name(p1);
        m.setPlayer2Name(p2);
        m.setResult(winner + "の勝ち");
        m.setWinnerName(winner);
        m.setUrl("url-" + System.nanoTime());
        return m;
    }
}

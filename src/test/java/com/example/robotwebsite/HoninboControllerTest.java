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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class HoninboControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MatchRepository matchRepository;

    @Test
    public void testHoninboMatchesPage() throws Exception {
        // 本因坊戦のテストデータを作成
        Match honinboMatch = new Match();
        honinboMatch.setMatchDate(LocalDate.now());
        honinboMatch.setMatchName("第80期本因坊戦五番勝負第1局");
        honinboMatch.setPlayer1Name("一力遼");
        honinboMatch.setPlayer2Name("芝野虎丸");
        honinboMatch.setUrl("honinbo-test-url");
        matchRepository.save(honinboMatch);

        // 別の棋戦のデータ
        Match otherMatch = new Match();
        otherMatch.setMatchDate(LocalDate.now());
        otherMatch.setMatchName("名人戦");
        otherMatch.setPlayer1Name("Player A");
        otherMatch.setPlayer2Name("Player B");
        otherMatch.setUrl("other-test-url");
        matchRepository.save(otherMatch);

        // 本因坊戦のタイトルで検索
        mockMvc.perform(get("/matches/title/本因坊戦"))
                .andExpect(status().isOk())
                .andExpect(view().name("match_list"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("本因坊戦 の対局一覧")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("第80期本因坊戦五番勝負第1局")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("名人戦"))));
    }
}

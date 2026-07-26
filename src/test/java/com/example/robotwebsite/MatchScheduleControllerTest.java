package com.example.robotwebsite;

import com.example.robotwebsite.entity.Match;
import com.example.robotwebsite.repository.MatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class MatchScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MatchRepository matchRepository;

    @BeforeEach
    public void setup() {
        matchRepository.deleteAll();
    }

    @Test
    public void testMatchSchedulePageWithNullSente() throws Exception {
        // Create a match with null sente values
        Match match = new Match();
        match.setMatchDate(LocalDate.now());
        match.setMatchName("Test Match");
        match.setPlayer1Name("Player A");
        match.setPlayer2Name("Player B");
        match.setPlayer1Sente(null); // This was causing the error
        match.setPlayer2Sente(null); // This was causing the error
        match.setUrl("test-url-1");
        matchRepository.save(match);

        mockMvc.perform(get("/match-schedule"))
                .andExpect(status().isOk())
                .andExpect(view().name("match_list"))
                .andExpect(model().attributeExists("matches"));
    }

    @Test
    public void testMatchSchedulePageWithNonNullSente() throws Exception {
        // Create a match with non-null sente values
        Match match = new Match();
        match.setMatchDate(LocalDate.now());
        match.setMatchName("Test Match 2");
        match.setPlayer1Name("Player C");
        match.setPlayer2Name("Player D");
        match.setPlayer1Sente(Boolean.TRUE);
        match.setPlayer2Sente(Boolean.FALSE);
        match.setUrl("test-url-2");
        matchRepository.save(match);

        mockMvc.perform(get("/match-schedule"))
                .andExpect(status().isOk())
                .andExpect(view().name("match_list"))
                .andExpect(model().attributeExists("matches"));
    }

    @Test
    public void testMatchResultsPage() throws Exception {
        // Create a match with result
        Match match = new Match();
        match.setMatchDate(LocalDate.now().minusDays(1));
        match.setMatchName("Test Match Result");
        match.setPlayer1Name("Player E");
        match.setPlayer2Name("Player F");
        match.setResult("Black wins");
        match.setWinnerName("Player E");
        match.setUrl("test-url-3");
        matchRepository.save(match);

        mockMvc.perform(get("/match-results"))
                .andExpect(status().isOk())
                .andExpect(view().name("match_list"))
                .andExpect(model().attributeExists("matches"));
    }

    @Test
    public void testMatchListPage() throws Exception {
        // Create a match
        Match match = new Match();
        match.setMatchDate(LocalDate.now());
        match.setMatchName("Test Match List");
        match.setPlayer1Name("Player G");
        match.setPlayer2Name("Player H");
        match.setUrl("test-url-4");
        matchRepository.save(match);

        mockMvc.perform(get("/match-list"))
                .andExpect(status().isOk())
                .andExpect(view().name("match_list"))
                .andExpect(model().attributeExists("matches"))
                .andExpect(model().attribute("title", "プロ棋士対局一覧"));
    }
}

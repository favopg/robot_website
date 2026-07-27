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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Test
    public void testMatchScheduleSorting() throws Exception {
        // Clear previous data
        matchRepository.deleteAll();

        LocalDate today = LocalDate.now();

        // Save matches in non-chronological order
        Match match1 = new Match();
        match1.setMatchDate(today.plusDays(5));
        match1.setMatchName("Later Match");
        match1.setPlayer1Name("A");
        match1.setPlayer2Name("B");
        match1.setUrl("url-1");
        matchRepository.save(match1);

        Match match2 = new Match();
        match2.setMatchDate(today.plusDays(2));
        match2.setMatchName("Earlier Match");
        match2.setPlayer1Name("C");
        match2.setPlayer2Name("D");
        match2.setUrl("url-2");
        matchRepository.save(match2);

        mockMvc.perform(get("/match-schedule"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("matches"))
                .andExpect(result -> {
                    List<Match> matches = (List<Match>) result.getModelAndView().getModel().get("matches");
                    assertTrue(matches.size() >= 2);
                    // Assert Ascending order: Earlier Match should come first
                    assertTrue(matches.get(0).getMatchDate().isBefore(matches.get(1).getMatchDate()) ||
                            matches.get(0).getMatchDate().isEqual(matches.get(1).getMatchDate()));
                    assertTrue(matches.get(0).getMatchName().equals("Earlier Match"));
                });
    }

    @Test
    public void testMatchScheduleAscendingOrder() throws Exception {
        LocalDate today = LocalDate.now();
        
        Match match1 = new Match();
        match1.setMatchDate(today.plusDays(5));
        match1.setMatchName("Later Match");
        match1.setPlayer1Name("P1");
        match1.setPlayer2Name("P2");
        match1.setUrl("url1");
        matchRepository.save(match1);

        Match match2 = new Match();
        match2.setMatchDate(today.plusDays(1));
        match2.setMatchName("Earlier Match");
        match2.setPlayer1Name("P3");
        match2.setPlayer2Name("P4");
        match2.setUrl("url2");
        matchRepository.save(match2);

        mockMvc.perform(get("/match-schedule"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("matches"))
                .andExpect(result -> {
                    List<Match> matches = (List<Match>) result.getModelAndView().getModel().get("matches");
                    assertTrue(matches.get(0).getMatchDate().isBefore(matches.get(1).getMatchDate()) 
                        || matches.get(0).getMatchDate().isEqual(matches.get(1).getMatchDate()), 
                        "Should be in ascending order");
                    assertTrue(matches.get(0).getMatchName().equals("Earlier Match"));
                });
    }

    @Test
    public void testMatchResultsDescendingOrder() throws Exception {
        LocalDate today = LocalDate.now();
        
        Match match1 = new Match();
        match1.setMatchDate(today.minusDays(5));
        match1.setMatchName("Older Result");
        match1.setPlayer1Name("P5");
        match1.setPlayer2Name("P6");
        match1.setResult("Result1");
        match1.setUrl("url3");
        matchRepository.save(match1);

        Match match2 = new Match();
        match2.setMatchDate(today.minusDays(1));
        match2.setMatchName("Newer Result");
        match2.setPlayer1Name("P7");
        match2.setPlayer2Name("P8");
        match2.setResult("Result2");
        match2.setUrl("url4");
        matchRepository.save(match2);

        mockMvc.perform(get("/match-results"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("matches"))
                .andExpect(result -> {
                    List<Match> matches = (List<Match>) result.getModelAndView().getModel().get("matches");
                    assertTrue(matches.get(0).getMatchDate().isAfter(matches.get(1).getMatchDate())
                        || matches.get(0).getMatchDate().isEqual(matches.get(1).getMatchDate()), 
                        "Should be in descending order");
                    assertTrue(matches.get(0).getMatchName().equals("Newer Result"));
                });
    }
}

package com.example.robotwebsite;

import com.example.robotwebsite.batch.KansaikiinPlayerScraper;
import com.example.robotwebsite.batch.NihonkiinPlayerScraper;
import com.example.robotwebsite.entity.Player;
import com.example.robotwebsite.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
public class PlayerScraperFixTest {

    @Autowired
    private NihonkiinPlayerScraper scraper;

    @MockBean
    private PlayerService playerService;

    @Test
    public void testScrapeChoChikun() {
        // Cho Chikun (趙　治勲) has birth date in profile text: "1956年（昭和31年）6月20日生"
        String name = "趙治勲";
        String detailUrl = "https://www.nihonkiin.or.jp/player/htm/ki000004.html";
        
        when(playerService.findByName(anyString())).thenReturn(Optional.empty());

        scraper.scrapePlayerDetail(name, detailUrl);

        ArgumentCaptor<Player> playerCaptor = ArgumentCaptor.forClass(Player.class);
        verify(playerService, atLeastOnce()).saveOrUpdate(playerCaptor.capture());
        
        Player savedPlayer = playerCaptor.getValue();
        assertEquals(LocalDate.of(1956, 6, 20), savedPlayer.getBirthDate());
        assertEquals("趙治勲", savedPlayer.getName());
    }
}

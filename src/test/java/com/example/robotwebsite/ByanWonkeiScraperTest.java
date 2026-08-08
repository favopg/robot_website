package com.example.robotwebsite;

import com.example.robotwebsite.batch.NihonkiinPlayerScraper;
import com.example.robotwebsite.entity.Player;
import com.example.robotwebsite.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class ByanWonkeiScraperTest {

    @Autowired
    private NihonkiinPlayerScraper scraper;

    @MockBean
    private PlayerService playerService;

    @Test
    public void testScrapeByanWonkei() {
        String name = "卞聞愷";
        String detailUrl = "https://www.nihonkiin.or.jp/player/htm/ki000449.html";
        
        when(playerService.findByName(anyString())).thenReturn(Optional.empty());

        // Perform scraping (logic only check via Mock)
        scraper.scrapePlayerDetail(name, detailUrl);

        ArgumentCaptor<Player> playerCaptor = ArgumentCaptor.forClass(Player.class);
        verify(playerService, atLeastOnce()).saveOrUpdate(playerCaptor.capture());
        
        Player savedPlayer = playerCaptor.getValue();
        assertEquals(name, savedPlayer.getName());
        assertEquals(detailUrl, savedPlayer.getProfileUrl());
        // Rank might be scraped from the page, but we ensure the URL is correct
    }
}

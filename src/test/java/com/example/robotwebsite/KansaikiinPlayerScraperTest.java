package com.example.robotwebsite;

import com.example.robotwebsite.batch.KansaikiinPlayerScraper;
import com.example.robotwebsite.entity.Player;
import com.example.robotwebsite.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class KansaikiinPlayerScraperTest {

    @Autowired
    private KansaikiinPlayerScraper scraper;

    @Autowired
    private PlayerRepository playerRepository;

    @Test
    public void testScrapeKansaikiinPlayer() {
        // 村川大介九段をテスト対象にする
        String playerName = "村川大介";
        
        boolean result = scraper.scrapeAndSavePlayer(playerName);
        assertTrue(result, "Should find and save player info");

        Optional<Player> playerOpt = playerRepository.findByName(playerName);
        assertTrue(playerOpt.isPresent(), "Player should be in DB");
        
        Player player = playerOpt.get();
        assertEquals(playerName, player.getName());
        assertEquals("関西棋院", player.getAffiliation());
        assertNotNull(player.getRank(), "Rank should be scraped");
        assertNotNull(player.getProfileUrl(), "Profile URL should be set");
        assertNotNull(player.getIconPath(), "Icon path should be set");
        assertTrue(player.getIconPath().startsWith("http"), "Icon path should be an absolute URL");
        
        System.out.println("[DEBUG_LOG] Scraped Player: " + player.getName());
        System.out.println("[DEBUG_LOG] Rank: " + player.getRank());
        System.out.println("[DEBUG_LOG] Affiliation: " + player.getAffiliation());
        System.out.println("[DEBUG_LOG] BirthPlace: " + player.getBirthPlace());
        System.out.println("[DEBUG_LOG] Profile URL: " + player.getProfileUrl());
        System.out.println("[DEBUG_LOG] Icon Path: " + player.getIconPath());
    }
}

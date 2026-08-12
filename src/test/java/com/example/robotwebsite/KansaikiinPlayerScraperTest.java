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
        
        // 生年月日が正しくパースされているか確認 (1990-12-14)
        assertEquals(java.time.LocalDate.of(1990, 12, 14), player.getBirthDate());
        
        // 読み仮名の取得確認
        assertEquals("むらかわ　だいすけ", player.getKanaName());
        
        System.out.println("[DEBUG_LOG] Scraped Player: " + player.getName());
        System.out.println("[DEBUG_LOG] BirthDate: " + player.getBirthDate());
    }

    @Test
    public void testScrapeKansaikiinPlayerYoSeiki() {
        // 余正麒九段をテスト対象にする
        String playerName = "余正麒";
        
        boolean result = scraper.scrapeAndSavePlayer(playerName);
        assertTrue(result, "Should find and save player info for Yo Seiki");

        Optional<Player> playerOpt = playerRepository.findByName(playerName);
        assertTrue(playerOpt.isPresent(), "Player should be in DB");
        
        Player player = playerOpt.get();
        assertEquals(playerName, player.getName());
        
        // 読み仮名の全角スペース保持
        assertEquals("よ　せいき", player.getKanaName());
        // 生年月日の西暦抽出 (1995-06-19)
        assertEquals(java.time.LocalDate.of(1995, 6, 19), player.getBirthDate());
        // 出身地のスペース除去 (台湾台北市)
        assertEquals("台湾台北市", player.getBirthPlace());

        System.out.println("[DEBUG_LOG] Scraped Player: " + player.getName());
        System.out.println("[DEBUG_LOG] Kana: " + player.getKanaName());
        System.out.println("[DEBUG_LOG] BirthDate: " + player.getBirthDate());
        System.out.println("[DEBUG_LOG] BirthPlace: " + player.getBirthPlace());
    }
}

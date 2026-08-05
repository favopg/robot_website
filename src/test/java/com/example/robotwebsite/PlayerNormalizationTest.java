package com.example.robotwebsite;

import com.example.robotwebsite.entity.Match;
import com.example.robotwebsite.entity.Player;
import com.example.robotwebsite.repository.PlayerRepository;
import com.example.robotwebsite.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PlayerNormalizationTest {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private PlayerRepository playerRepository;

    @Test
    public void testNormalizeName() {
        assertEquals("一力遼", playerService.normalizeName("一力遼 名人"));
        assertEquals("芝野虎丸", playerService.normalizeName("芝野虎丸 棋聖"));
        assertEquals("井山裕太", playerService.normalizeName("井山裕太 王座・碁聖"));
        assertEquals("仲邑菫", playerService.normalizeName("仲邑菫 三段"));
        assertEquals("竹下奈那", playerService.normalizeName("竹下奈那 初段"));
        assertEquals("上野梨紗", playerService.normalizeName("上野梨紗 扇興杯"));
        assertEquals("藤沢里菜", playerService.normalizeName("藤沢里菜 女流本因坊"));
        assertEquals("上野愛咲美", playerService.normalizeName("上野愛咲美 女流名人"));
        assertEquals("栁原咲輝", playerService.normalizeName("柳原咲輝"));
        assertEquals("栁原咲輝", playerService.normalizeName("柳原咲輝 二段"));
        assertEquals("名無し", playerService.normalizeName("名無し"));
        assertNull(playerService.normalizeName(null));
    }

    @Test
    public void testIconMappingWithNormalization() {
        // Setup player with icon
        Player player = new Player();
        player.setName("一力遼");
        player.setIconPath("/images/players/一力遼.jpg");
        playerRepository.save(player);

        // Test matching with title
        Match match = new Match();
        match.setPlayer1Name("一力遼 名人");
        match.setPlayer2Name("芝野虎丸 棋聖");

        // We simulate the behavior in IndexController.updatePlayerIcon
        String normalizedName = playerService.normalizeName(match.getPlayer1Name());
        assertEquals("一力遼", normalizedName);

        String iconPath = playerRepository.findByName(normalizedName)
                .map(Player::getIconPath)
                .orElse(null);

        assertEquals("/images/players/一力遼.jpg", iconPath);
    }
}

package com.example.robotwebsite;

import com.example.robotwebsite.entity.Player;
import com.example.robotwebsite.repository.PlayerRepository;
import com.example.robotwebsite.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PlayerServiceLikeTest {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private PlayerRepository playerRepository;

    @Test
    void testIncrementLikes() {
        String playerName = "Test Player";
        Player player = new Player();
        player.setName(playerName);
        player.setLikesCount(0);
        playerRepository.saveAndFlush(player);

        int count1 = playerService.incrementLikes(playerName);
        assertEquals(1, count1);

        int count2 = playerService.incrementLikes(playerName);
        assertEquals(2, count2);

        Player savedPlayer = playerRepository.findByName(playerName).orElseThrow();
        assertEquals(2, savedPlayer.getLikesCount());
    }

    @Test
    void testDecrementLikes() {
        String playerName = "Test Player Dec";
        Player player = new Player();
        player.setName(playerName);
        player.setLikesCount(5);
        playerRepository.saveAndFlush(player);

        int count1 = playerService.decrementLikes(playerName);
        assertEquals(4, count1);

        int count2 = playerService.decrementLikes(playerName);
        assertEquals(3, count2);

        Player savedPlayer = playerRepository.findByName(playerName).orElseThrow();
        assertEquals(3, savedPlayer.getLikesCount());
    }

    @Test
    void testDecrementLikesNotBelowZero() {
        String playerName = "Test Player Zero";
        Player player = new Player();
        player.setName(playerName);
        player.setLikesCount(0);
        playerRepository.saveAndFlush(player);

        int count = playerService.decrementLikes(playerName);
        assertEquals(0, count);

        Player savedPlayer = playerRepository.findByName(playerName).orElseThrow();
        assertEquals(0, savedPlayer.getLikesCount());
    }
}

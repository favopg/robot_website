package com.example.robotwebsite;

import com.example.robotwebsite.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class PlayerNameNormalizationTest {

    @Autowired
    private PlayerService playerService;

    @Test
    void testNormalizeNameWithSpacesAndTitles() {
        // 名誉称号などスペース付きの名前
        assertEquals("趙治勲", playerService.normalizeName("趙治勲 名誉名人"));
        assertEquals("趙治勲", playerService.normalizeName("趙治勲　名誉名人")); // 全角スペース
        assertEquals("一力遼", playerService.normalizeName("一力遼 名人"));
        assertEquals("井山裕太", playerService.normalizeName("井山裕太 王座"));
        
        // 通常の段位付き
        assertEquals("一力遼", playerService.normalizeName("一力遼九段"));
        assertEquals("一力遼", playerService.normalizeName("一力遼 九段"));
        
        // 特殊な例
        assertEquals("石田芳夫", playerService.normalizeName("二十四世本因坊秀芳"));
        assertEquals("栁原咲輝", playerService.normalizeName("柳原咲輝"));
    }
}

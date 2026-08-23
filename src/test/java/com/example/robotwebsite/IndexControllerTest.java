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
public class IndexControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MatchRepository matchRepository;

    @Test
    public void testIndexPageDoesNotContainYouTubeLives() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeDoesNotExist("youtubeLives"));
    }

    @Test
    public void testIndexPagePagingUpperAndLower() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<!-- 上部ページングナビゲーション -->")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<!-- ページングナビゲーション -->")));
    }

    @Test
    public void testKifuIntroPage() throws Exception {
        mockMvc.perform(get("/kifu-intro"))
                .andExpect(status().isOk())
                .andExpect(view().name("kifu_intro"))
                .andExpect(model().attribute("title", "棋譜紹介"));
    }

    @Test
    public void testIndexPageContainsKifuIntroSection() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("棋譜紹介ページ")));
    }
}

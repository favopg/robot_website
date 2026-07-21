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
    public void testYoutubeSchedulePage() throws Exception {
        mockMvc.perform(get("/youtube-schedule"))
                .andExpect(status().isOk())
                .andExpect(view().name("youtube_list"))
                .andExpect(model().attributeExists("youtubeLives"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("YouTube 配信予定（日本棋院）")));
    }
}

package com.example.robotwebsite;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class IndexControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testIndexPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    public void testRecommendedKifuPage() throws Exception {
        mockMvc.perform(get("/recommended-kifu"))
                .andExpect(status().isOk())
                .andExpect(view().name("recommended_kifu"))
                .andExpect(model().attribute("title", "おすすめ棋譜"));
    }

    @Test
    public void testKifuListPage() throws Exception {
        mockMvc.perform(get("/kifu-list"))
                .andExpect(status().isOk())
                .andExpect(view().name("kifu_list"))
                .andExpect(model().attribute("title", "棋譜一覧"));
    }
}

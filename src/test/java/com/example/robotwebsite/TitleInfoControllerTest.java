package com.example.robotwebsite;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TitleInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testTitleInfoPages() throws Exception {
        String[] titles = {"kisei", "meijin", "honinbo", "ouza", "tengen", "gosei", "judan"};
        String[] displayNames = {"棋聖戦", "名人戦", "本因坊戦", "王座戦", "天元戦", "碁聖戦", "十段戦"};

        for (int i = 0; i < titles.length; i++) {
            mockMvc.perform(get("/" + titles[i] + "-info"))
                    .andExpect(status().isOk())
                    .andExpect(view().name(titles[i] + "_info"))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString(displayNames[i] + "の仕組み")));
        }
    }
}

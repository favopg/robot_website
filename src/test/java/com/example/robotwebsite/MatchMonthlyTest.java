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
public class MatchMonthlyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MatchRepository matchRepository;

    @Test
    public void testMatchResultsMonthly() throws Exception {
        // 2026年6月のデータ
        Match match6 = new Match();
        match6.setMatchDate(LocalDate.of(2026, 6, 15));
        match6.setMatchName("June Match");
        match6.setPlayer1Name("Player A");
        match6.setPlayer2Name("Player B");
        match6.setResult("Aの勝ち");
        match6.setWinnerName("Player A");
        match6.setUrl("url-june");
        matchRepository.save(match6);

        // 2026年5月のデータ
        Match match5 = new Match();
        match5.setMatchDate(LocalDate.of(2026, 5, 20));
        match5.setMatchName("May Match");
        match5.setPlayer1Name("Player C");
        match5.setPlayer2Name("Player D");
        match5.setResult("Cの勝ち");
        match5.setWinnerName("Player C");
        match5.setUrl("url-may");
        matchRepository.save(match5);

        // 6月を指定してリクエスト
        mockMvc.perform(get("/match-results").param("year", "2026").param("month", "6"))
                .andExpect(status().isOk())
                .andExpect(view().name("match_list"))
                .andExpect(model().attributeExists("matches"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("2026年6月")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("June Match")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("May Match"))));

        // 5月を指定してリクエスト
        mockMvc.perform(get("/match-results").param("year", "2026").param("month", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("match_list"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("2026年5月")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("May Match")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("June Match"))));
    }

    @Test
    public void testMatchResultsMonthlyLinksOnlyForExistingData() throws Exception {
        // 全データを削除してクリーンな状態でテスト
        matchRepository.deleteAll();

        // 現在の日付を取得（テスト実行時の月を基準にする）
        LocalDate now = LocalDate.now();
        LocalDate lastMonth = now.minusMonths(1);

        // 先月のデータのみ作成
        Match matchLastMonth = new Match();
        matchLastMonth.setMatchDate(lastMonth.withDayOfMonth(10));
        matchLastMonth.setMatchName("Last Month Match");
        matchLastMonth.setPlayer1Name("Player A");
        matchLastMonth.setPlayer2Name("Player B");
        matchLastMonth.setResult("Aの勝ち");
        matchLastMonth.setUrl("url-last-month");
        matchRepository.save(matchLastMonth);

        // 今月のデータ（結果なし）を作成
        Match matchThisMonthNoResult = new Match();
        matchThisMonthNoResult.setMatchDate(now.withDayOfMonth(1));
        matchThisMonthNoResult.setMatchName("This Month No Result");
        matchThisMonthNoResult.setPlayer1Name("Player C");
        matchThisMonthNoResult.setPlayer2Name("Player D");
        matchThisMonthNoResult.setUrl("url-this-month-no-result");
        matchRepository.save(matchThisMonthNoResult);

        // リクエスト実行
        mockMvc.perform(get("/match-results"))
                .andExpect(status().isOk())
                .andExpect(view().name("match_list"))
                // 先月のリンクは存在するはず
                .andExpect(content().string(org.hamcrest.Matchers.containsString(lastMonth.getYear() + "年" + lastMonth.getMonthValue() + "月")))
                // 今月のリンクは存在しないはず（結果がないため）
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(now.getYear() + "年" + now.getMonthValue() + "月"))));
    }
}

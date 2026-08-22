package com.example.robotwebsite;

import com.example.robotwebsite.batch.FavoritePlayerExportTasklet;
import com.example.robotwebsite.entity.Player;
import com.example.robotwebsite.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FavoritePlayerExportTaskletTest {

    @Test
    public void testExecute(@TempDir Path tempDir) throws Exception {
        PlayerRepository playerRepository = mock(PlayerRepository.class);

        Player player1 = new Player();
        player1.setId(1L);
        player1.setName("一力遼");
        player1.setGender("男性");
        player1.setRank("九段");
        player1.setBirthPlace("宮城県仙台市");
        player1.setBirthDate(LocalDate.of(1997, 6, 10));
        player1.setAffiliation("日本棋院東京本院");
        player1.setProfileUrl("https://example.com/ichiriki");
        player1.setIconPath("/images/players/ichiriki.jpg");
        player1.setKanaName("いちりき りょう");
        player1.setLikesCount(10);
        player1.setFavorite(true);
        player1.setRecentStats("10勝2敗");
        player1.setRecentMatches("vs 芝野虎丸 (勝)");

        when(playerRepository.findByIsFavoriteTrue()).thenReturn(Arrays.asList(player1));

        FavoritePlayerExportTasklet tasklet = new FavoritePlayerExportTasklet(playerRepository);
        tasklet.setCsvDir(tempDir.toString());
        tasklet.setCsvFilename("favorite_players.csv");

        StepContribution contribution = mock(StepContribution.class);
        ChunkContext chunkContext = mock(ChunkContext.class);

        RepeatStatus status = tasklet.execute(contribution, chunkContext);
        assertEquals(RepeatStatus.FINISHED, status);

        File exportedFile = tempDir.resolve("favorite_players.csv").toFile();
        assertTrue(exportedFile.exists());

        List<String> lines = Files.readAllLines(exportedFile.toPath());
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).startsWith("id,name,gender,rank,birth_place,birth_date"));
        assertTrue(lines.get(1).contains("一力遼"));
        assertTrue(lines.get(1).contains("true"));

        // Test overwrite
        RepeatStatus status2 = tasklet.execute(contribution, chunkContext);
        assertEquals(RepeatStatus.FINISHED, status2);
        assertTrue(exportedFile.exists());
    }
}

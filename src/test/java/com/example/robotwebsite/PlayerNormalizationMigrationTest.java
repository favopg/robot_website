package com.example.robotwebsite;

import com.example.robotwebsite.batch.PlayerNormalizationTasklet;
import com.example.robotwebsite.entity.Match;
import com.example.robotwebsite.entity.Player;
import com.example.robotwebsite.repository.MatchRepository;
import com.example.robotwebsite.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PlayerNormalizationMigrationTest {

    @Autowired
    private PlayerNormalizationTasklet tasklet;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MatchRepository matchRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void testMigration() throws Exception {
        // 1. Setup dirty data
        // Player with title
        Player p1 = new Player();
        p1.setName("一力遼 名人");
        p1.setLikesCount(10);
        playerRepository.save(p1);

        // Match with titles
        Match m1 = new Match();
        m1.setMatchDate(LocalDate.now());
        m1.setMatchName("テスト対局");
        m1.setPlayer1Name("一力遼 名人");
        m1.setPlayer2Name("名前A 称号");
        m1.setWinnerName("一力遼 名人");
        m1.setUrl("http://test.com/1");
        matchRepository.save(m1);

        // Player that already has normalized name (to test merge)
        Player p2 = new Player();
        p2.setName("名前A");
        p2.setLikesCount(5);
        playerRepository.save(p2);

        // Player with title that will merge into p2
        Player p3 = new Player();
        p3.setName("名前A 称号");
        p3.setLikesCount(3);
        playerRepository.save(p3);

        playerRepository.flush(); // Ensure data is written

        // 2. Run migration
        tasklet.execute(null, null);

        // 3. Verify
        // p1 should be normalized
        assertFalse(playerRepository.findByName("一力遼 名人").isPresent());
        assertTrue(playerRepository.findByName("一力遼").isPresent());
        assertEquals(10, playerRepository.findByName("一力遼").get().getLikesCount());

        // p3 should be merged into p2
        assertFalse(playerRepository.findByName("名前A 称号").isPresent());
        assertTrue(playerRepository.findByName("名前A").isPresent());
        assertEquals(8, playerRepository.findByName("名前A").get().getLikesCount()); // 5 + 3

        // match should be normalized
        Match updatedMatch = matchRepository.findByUrl("http://test.com/1").get();
        assertEquals("一力遼", updatedMatch.getPlayer1Name());
        assertEquals("名前A", updatedMatch.getPlayer2Name());
        assertEquals("一力遼", updatedMatch.getWinnerName());
    }

    @Test
    public void testMergeLogic() throws Exception {
        Player p2 = new Player();
        p2.setName("名前B");
        p2.setLikesCount(5);
        playerRepository.save(p2);

        Player p3 = new Player();
        p3.setName("名前B 称号");
        p3.setLikesCount(3);
        playerRepository.save(p3);
        
        playerRepository.flush();

        // tasklet実行前に一旦コミット（トランザクション終了）したいが、@Transactionalがついているので難しい
        // そのため手動でクリアするなどの対応が必要な場合がある
        
        tasklet.execute(null, null);
        
        // 検証前にEntityManagerをクリアしてDBから再取得させる
        entityManager.flush();
        entityManager.clear();

        assertFalse(playerRepository.existsById(p3.getId()), "p3 should be deleted");
        Optional<Player> pB = playerRepository.findByName("名前B");
        assertTrue(pB.isPresent());
        assertEquals(8, pB.get().getLikesCount());
    }
}

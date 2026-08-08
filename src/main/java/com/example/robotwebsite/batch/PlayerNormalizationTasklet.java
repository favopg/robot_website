package com.example.robotwebsite.batch;

import com.example.robotwebsite.entity.Match;
import com.example.robotwebsite.entity.Player;
import com.example.robotwebsite.repository.MatchRepository;
import com.example.robotwebsite.repository.PlayerRepository;
import com.example.robotwebsite.service.PlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;

@Component
public class PlayerNormalizationTasklet implements Tasklet {

    private static final Logger logger = LoggerFactory.getLogger(PlayerNormalizationTasklet.class);
    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final PlayerService playerService;
    private final JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    public PlayerNormalizationTasklet(PlayerRepository playerRepository, MatchRepository matchRepository, PlayerService playerService, JdbcTemplate jdbcTemplate) {
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
        this.playerService = playerService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.info("Starting player name normalization migration...");

        // 1. players テーブルの正規化
        List<Player> allPlayers = playerRepository.findAll();
        for (Player p : allPlayers) {
            String originalName = p.getName();
            String normalizedName = playerService.normalizeName(originalName);
            
            Optional<Player> currentOpt = playerRepository.findById(p.getId());
            if (currentOpt.isEmpty()) continue;
            Player currentPlayer = currentOpt.get();

            if (originalName.equals(normalizedName)) continue;

            logger.info("Processing player normalization: id={} '{}' -> '{}'", currentPlayer.getId(), originalName, normalizedName);

            Optional<Player> existingOpt = playerRepository.findByName(normalizedName);
            if (existingOpt.isPresent()) {
                Player existing = existingOpt.get();
                if (!existing.getId().equals(currentPlayer.getId())) {
                    logger.info("Merging player id={} into existing id={} ({})", currentPlayer.getId(), existing.getId(), existing.getName());
                    existing.setLikesCount(existing.getLikesCount() + currentPlayer.getLikesCount());
                    if (existing.getRank() == null) existing.setRank(currentPlayer.getRank());
                    if (existing.getBirthDate() == null) existing.setBirthDate(currentPlayer.getBirthDate());
                    playerRepository.save(existing);
                    
                    playerRepository.delete(currentPlayer);
                }
            } else {
                logger.info("Updating player name id={} to '{}'", currentPlayer.getId(), normalizedName);
                currentPlayer.setName(normalizedName);
                playerRepository.save(currentPlayer);
            }
            playerRepository.flush();
        }

        // 2. matches テーブルの正規化
        List<Match> matches = matchRepository.findAll();
        for (Match match : matches) {
            boolean changed = false;
            
            String p1Original = match.getPlayer1Name();
            String p1Normalized = playerService.normalizeName(p1Original);
            if (p1Original != null && !p1Original.equals(p1Normalized)) {
                match.setPlayer1Name(p1Normalized);
                changed = true;
            }

            String p2Original = match.getPlayer2Name();
            String p2Normalized = playerService.normalizeName(p2Original);
            if (p2Original != null && !p2Original.equals(p2Normalized)) {
                match.setPlayer2Name(p2Normalized);
                changed = true;
            }

            String winnerOriginal = match.getWinnerName();
            String winnerNormalized = playerService.normalizeName(winnerOriginal);
            if (winnerOriginal != null && !winnerOriginal.equals(winnerNormalized)) {
                match.setWinnerName(winnerNormalized);
                changed = true;
            }

            if (changed) {
                matchRepository.save(match);
            }
        }

        logger.info("Player name normalization migration completed.");
        return RepeatStatus.FINISHED;
    }
}

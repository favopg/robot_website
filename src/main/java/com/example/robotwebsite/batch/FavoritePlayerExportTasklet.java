package com.example.robotwebsite.batch;

import com.example.robotwebsite.entity.Player;
import com.example.robotwebsite.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class FavoritePlayerExportTasklet implements Tasklet {

    private static final Logger logger = LoggerFactory.getLogger(FavoritePlayerExportTasklet.class);

    private final PlayerRepository playerRepository;

    @Value("${favorite.player.csv.dir:C:\\Users\\favor\\OneDrive\\Desktop\\favokishi}")
    private String csvDir;

    @Value("${favorite.player.csv.filename:favorite_players.csv}")
    private String csvFilename;

    public FavoritePlayerExportTasklet(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.info("Starting FavoritePlayerExportTasklet...");

        List<Player> favoritePlayers = playerRepository.findByIsFavoriteTrue();
        logger.info("Found {} favorite players to export.", favoritePlayers.size());

        File dir = new File(csvDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            logger.info("Directory {} created: {}", csvDir, created);
        }

        File csvFile = new File(dir, csvFilename);
        logger.info("Exporting favorite players to CSV file: {}", csvFile.getAbsolutePath());

        // 上書き保存 (false: overwrite)
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(csvFile, false), StandardCharsets.UTF_8))) {

            // Header
            writer.write(String.join(",",
                    "id",
                    "name",
                    "gender",
                    "rank",
                    "birth_place",
                    "birth_date",
                    "affiliation",
                    "profile_url",
                    "icon_path",
                    "kana_name",
                    "likes_count",
                    "is_favorite",
                    "updated_at",
                    "recent_stats",
                    "recent_matches"
            ));
            writer.newLine();

            // Rows
            for (Player player : favoritePlayers) {
                String line = String.join(",",
                        escapeCsv(player.getId()),
                        escapeCsv(player.getName()),
                        escapeCsv(player.getGender()),
                        escapeCsv(player.getRank()),
                        escapeCsv(player.getBirthPlace()),
                        escapeCsv(player.getBirthDate()),
                        escapeCsv(player.getAffiliation()),
                        escapeCsv(player.getProfileUrl()),
                        escapeCsv(player.getIconPath()),
                        escapeCsv(player.getKanaName()),
                        escapeCsv(player.getLikesCount()),
                        escapeCsv(player.isFavorite()),
                        escapeCsv(player.getUpdatedAt()),
                        escapeCsv(player.getRecentStats()),
                        escapeCsv(player.getRecentMatches())
                );
                writer.write(line);
                writer.newLine();
            }
            writer.flush();
        }

        logger.info("FavoritePlayerExportTasklet completed successfully.");
        return RepeatStatus.FINISHED;
    }

    private String escapeCsv(Object value) {
        if (value == null) {
            return "";
        }
        String str = value.toString();
        if (str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r")) {
            return "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }

    public void setCsvDir(String csvDir) {
        this.csvDir = csvDir;
    }

    public void setCsvFilename(String csvFilename) {
        this.csvFilename = csvFilename;
    }
}

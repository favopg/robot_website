package com.example.robotwebsite.batch;

import com.example.robotwebsite.entity.Match;
import com.example.robotwebsite.repository.MatchRepository;
import com.example.robotwebsite.service.MatchService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class NihonkiinMatchScraperTasklet implements Tasklet {

    private static final Logger logger = LoggerFactory.getLogger(NihonkiinMatchScraperTasklet.class);
    private final MatchRepository matchRepository;
    private final MatchService matchService;
    private final JdbcTemplate jdbcTemplate;
    private static final String URL = "https://www.nihonkiin.or.jp/match/2week.html";

    public NihonkiinMatchScraperTasklet(MatchRepository matchRepository, MatchService matchService, JdbcTemplate jdbcTemplate) {
        this.matchRepository = matchRepository;
        this.matchService = matchService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.info("Starting Nihonkiin Match Scraping...");

        Long siteSourceId = getSiteSourceId();
        if (siteSourceId == null) {
            logger.error("Site source '日本棋院 棋戦' not found in database. Skipping scraping.");
            return RepeatStatus.FINISHED;
        }

        try {
            Document doc = Jsoup.connect(URL).get();
            Elements tables = doc.select("table");
            
            // Results table (usually the first one)
            if (tables.size() >= 1) {
                parseAndSaveMatches(tables.get(0), true, siteSourceId);
            }
            
            // Schedule table (usually the second one)
            if (tables.size() >= 2) {
                parseAndSaveMatches(tables.get(1), false, siteSourceId);
            }

        } catch (IOException e) {
            logger.error("Failed to fetch Nihonkiin match page", e);
            throw e;
        }

        return RepeatStatus.FINISHED;
    }

    private Long getSiteSourceId() {
        try {
            String sql = "SELECT id FROM site_sources WHERE site_name = '日本棋院 棋戦' LIMIT 1";
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            return (Long) result.get("id");
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            logger.error("Error fetching site_source_id", e);
            return null;
        }
    }

    private void parseAndSaveMatches(Element table, boolean isResult, Long siteSourceId) {
        Elements rows = table.select("tr");
        LocalDate currentDate = null;
        int year = LocalDate.now().getYear();

        for (Element row : rows) {
            Elements cells = row.select("td");
            if (cells.isEmpty()) continue;

            String rowText = row.text().trim();
            // Pattern for date: starts with something like "6月12日"
            Pattern datePattern = Pattern.compile("^\\D*(\\d+)\\D+(\\d+)\\D+.*");
            Matcher dateMatcher = datePattern.matcher(rowText);

            if (cells.size() == 1 && dateMatcher.matches()) {
                int month = Integer.parseInt(dateMatcher.group(1));
                int day = Integer.parseInt(dateMatcher.group(2));
                currentDate = LocalDate.of(year, month, day);
                
                // Adjust year if necessary
                if (currentDate.isAfter(LocalDate.now().plusMonths(6))) {
                    currentDate = currentDate.minusYears(1);
                } else if (currentDate.isBefore(LocalDate.now().minusMonths(6))) {
                    currentDate = currentDate.plusYears(1);
                }
                continue;
            }

            if (currentDate == null) continue;

            try {
                Match match = new Match();
                match.setMatchDate(currentDate);
                match.setSiteSourceId(siteSourceId);

                if (isResult && cells.size() >= 5) { // Result row usually has 6 cells
                    // Result row format: matchName, senteMark1, player1, resultDescription, senteMark2, player2
                    String matchName = cells.get(0).text().trim();
                    if (matchName.length() > 500) matchName = matchName.substring(0, 500);
                    match.setMatchName(matchName);
                    
                    String sente1 = cells.get(1).text().trim();
                    String p1 = cells.get(2).text().trim();
                    String result = cells.get(3).text().trim();
                    String sente2 = (cells.size() >= 6) ? cells.get(4).text().trim() : "";
                    String p2 = cells.get(cells.size() - 1).text().trim();

                    if (p1.isEmpty() || p2.isEmpty()) continue;

                    match.setPlayer1Name(p1);
                    match.setPlayer1Sente("△".equals(sente1));
                    match.setResult(result);
                    match.setPlayer2Name(p2);
                    match.setPlayer2Sente("△".equals(sente2));
                    match.setWinnerName(p1);
                    
                } else if (!isResult && cells.size() >= 3) {
                    // Schedule row format: matchName, player1, (empty/vs), player2
                    match.setMatchName(cells.get(0).text().trim());
                    match.setPlayer1Name(cells.get(1).text().trim());
                    match.setPlayer2Name(cells.get(cells.size() - 1).text().trim());

                    if (match.getPlayer1Name().isEmpty() || match.getPlayer2Name().isEmpty()) continue;
                } else {
                    continue;
                }

                String uniqueKey = String.format("%s_%s_%s_%s", 
                    match.getMatchDate(), 
                    match.getMatchName(), 
                    match.getPlayer1Name(), 
                    match.getPlayer2Name());
                if (uniqueKey.length() > 500) uniqueKey = uniqueKey.substring(0, 500);
                match.setUrl(uniqueKey);

                saveOrUpdateMatch(match);

            } catch (Exception e) {
                logger.error("Error parsing row", e);
            }
        }
    }

    private void saveOrUpdateMatch(Match match) {
        try {
            matchService.saveOrUpdate(match);
        } catch (Exception e) {
            logger.error("Error saving match: " + match.getUrl(), e);
        }
    }
}

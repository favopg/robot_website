package com.example.robotwebsite.batch;

import com.example.robotwebsite.entity.Player;
import com.example.robotwebsite.service.PlayerService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class NihonkiinPlayerScraper {

    private static final Logger logger = LoggerFactory.getLogger(NihonkiinPlayerScraper.class);
    private final PlayerService playerService;
    
    public NihonkiinPlayerScraper(PlayerService playerService) {
        this.playerService = playerService;
    }
    
    public void scrapeAndSavePlayer(String playerName) {
        if (playerName == null || playerName.isEmpty()) return;
        
        Optional<Player> existingPlayer = playerService.findByName(playerName);
        if (existingPlayer.isPresent()) {
            // If essential info is missing, re-scrape
            Player p = existingPlayer.get();
            if (p.getGender() != null && p.getRank() != null) {
                return;
            }
            logger.info("Re-scraping player profile to fill missing info: " + playerName);
        } else {
            logger.info("Searching for player profile: " + playerName);
        }
        
        try {
            // Remove rank from the end of the name (e.g., "Name Rank" -> "Name")
            String searchName = playerName.replaceAll("[\\s\u3000]*([一二三四五六七八九十]|\\d+)段$", "").trim();
            
            // 1. Search in /player/dan/ (high reliability)
            String danUrl = "https://www.nihonkiin.or.jp/player/dan/";
            Document danDoc = Jsoup.connect(danUrl).get();
            Elements links = danDoc.select("a[href*=/player/htm/ki]");
            
            for (Element link : links) {
                // Match name (remove all whitespace for comparison)
                String linkText = link.text().replaceAll("[\\s\u3000]+", "");
                String normalizedSearchName = searchName.replaceAll("[\\s\u3000]+", "");
                if (linkText.equals(normalizedSearchName) || link.attr("href").contains(normalizedSearchName)) {
                    String detailUrl = link.absUrl("href");
                    scrapePlayerDetail(playerName, detailUrl);
                    return;
                }
            }
            
            logger.warn("Profile URL not found for player: " + playerName + " (searched in dan list as: " + searchName + ")");
        } catch (org.jsoup.HttpStatusException e) {
            logger.error("HTTP error searching player: " + playerName + " - Status=" + e.getStatusCode() + ", URL=" + e.getUrl());
        } catch (Exception e) {
            logger.error("Error searching player: " + playerName, e);
        }
    }

    public void scrapePlayerDetail(String playerName, String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            Player player = new Player();
            player.setName(playerName);
            player.setProfileUrl(url);

            Element rankElement = doc.selectFirst("div.rank");
            if (rankElement != null) {
                player.setRank(rankElement.text().trim());
            }

            // Target multiple possible table classes
            Elements tables = doc.select("table.inter-table, table.table1, table.table-rank");
            for (Element table : tables) {
                Elements rows = table.select("tr");
                for (Element row : rows) {
                    String th = row.select("th").text().trim();
                    String td = row.select("td").text().trim();

                    if (th.contains("Gender") || th.contains("性別")) {
                        player.setGender(td);
                    } else if (th.contains("Place") || th.contains("出身地")) {
                        player.setBirthPlace(td);
                    } else if (th.contains("Affiliation") || th.contains("所属")) {
                        player.setAffiliation(td);
                    } else if (th.contains("Rank") || th.contains("棋士段位")) {
                        player.setRank(td);
                    }
                }
            }

            Element imgElement = doc.selectFirst("div.player-photo img");
            if (imgElement != null) {
                String src = imgElement.absUrl("src");
                player.setIconPath(src);
            }

            playerService.saveOrUpdate(player);
            logger.info("Saved player info for: " + playerName);

        } catch (IOException e) {
            logger.error("Failed to fetch player detail page: " + url, e);
        }
    }
}

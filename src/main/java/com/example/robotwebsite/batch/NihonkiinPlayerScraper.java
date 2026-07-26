package com.example.robotwebsite.batch;

import com.example.robotwebsite.entity.Player;
import com.example.robotwebsite.repository.PlayerRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class NihonkiinPlayerScraper {

    private static final Logger logger = LoggerFactory.getLogger(NihonkiinPlayerScraper.class);
    private final PlayerRepository playerRepository;

    public NihonkiinPlayerScraper(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public void scrapeAndSavePlayer(String playerName) {
        if (playerName == null || playerName.isEmpty()) return;

        Optional<Player> existingPlayer = playerRepository.findByName(playerName);
        if (existingPlayer.isPresent()) {
            return;
        }

        logger.info("Searching for player profile: " + playerName);
        
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
            Player player = playerRepository.findByName(playerName).orElse(new Player());
            player.setName(playerName);
            player.setProfileUrl(url);

            Element rankElement = doc.selectFirst("div.rank");
            if (rankElement != null) {
                player.setRank(rankElement.text().trim());
            }

            Elements tables = doc.select("table.inter-table");
            for (Element table : tables) {
                Elements rows = table.select("tr");
                for (Element row : rows) {
                    String th = row.select("th").text().trim();
                    String td = row.select("td").text().trim();

                    // Using Unicode escapes for "性別", "生年月日", "出身地", "師匠", "門下", "所属", "棋士段位"
                    if (th.contains("Gender") || th.contains("\u6027\u5225")) {
                        player.setGender(td);
                    } else if (th.contains("Birthday") || th.contains("\u751f\u5e74\u6708\u65e5")) {
                        player.setBirthDate(parseJapaneseDate(td));
                    } else if (th.contains("Place") || th.contains("\u51fa\u8eab\u5730")) {
                        player.setBirthPlace(td);
                    } else if (th.contains("Master") || th.contains("\u5e2b\u5320") || th.contains("\u9580\u4e0b")) {
                        player.setMaster(td);
                    } else if (th.contains("Affiliation") || th.contains("\u6240\u5c5e")) {
                        player.setAffiliation(td);
                    } else if (th.contains("Rank") || th.contains("\u68cb\u58eb\u6bb5\u4f4d")) {
                        player.setRank(td);
                    }
                }
            }

            // If birth date is still null, try searching in the profile section
            if (player.getBirthDate() == null) {
                Element profileText = doc.selectFirst("div.profile-text");
                if (profileText != null) {
                    player.setBirthDate(parseJapaneseDate(profileText.text()));
                } else {
                    // Try searching in the entire document body for a date pattern followed by "生"
                    player.setBirthDate(parseJapaneseDate(doc.body().text()));
                }
            }
            
            Element imgElement = doc.selectFirst("div.player-photo img");
            if (imgElement != null) {
                String src = imgElement.absUrl("src");
                player.setIconPath(src);
            }

            playerRepository.save(player);
            logger.info("Saved player info for: " + playerName);

        } catch (IOException e) {
            logger.error("Failed to fetch player detail page: " + url, e);
        }
    }

    private LocalDate parseJapaneseDate(String dateStr) {
        try {
            // Match digits for year, month, day
            Pattern pattern = Pattern.compile("(\\d+).+(\\d+).+(\\d+).");
            Matcher matcher = pattern.matcher(dateStr);
            if (matcher.find()) {
                int year = Integer.parseInt(matcher.group(1));
                int month = Integer.parseInt(matcher.group(2));
                int day = Integer.parseInt(matcher.group(3));
                return LocalDate.of(year, month, day);
            }
        } catch (Exception e) {
            logger.warn("Failed to parse date: " + dateStr);
        }
        return null;
    }
}
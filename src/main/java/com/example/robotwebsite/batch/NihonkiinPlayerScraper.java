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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class NihonkiinPlayerScraper {

    private static final Logger logger = LoggerFactory.getLogger(NihonkiinPlayerScraper.class);
    private final PlayerService playerService;
    private final KansaikiinPlayerScraper kansaikiinPlayerScraper;
    
    public NihonkiinPlayerScraper(PlayerService playerService, KansaikiinPlayerScraper kansaikiinPlayerScraper) {
        this.playerService = playerService;
        this.kansaikiinPlayerScraper = kansaikiinPlayerScraper;
    }
    
    public void scrapeAndSavePlayer(String playerName) {
        if (playerName == null || playerName.isEmpty()) return;

        String searchName = playerService.normalizeName(playerName);
        
        Optional<Player> existingPlayer = playerService.findByName(searchName);
        if (existingPlayer.isPresent()) {
            // If essential info is missing, re-scrape
            Player p = existingPlayer.get();
            if (p.getGender() != null && p.getRank() != null && p.getIconPath() != null && p.getBirthDate() != null && p.getKanaName() != null && !p.getKanaName().isEmpty()) {
                return;
            }
            logger.info("Re-scraping player profile to fill missing info: " + searchName);
        } else {
            logger.info("Searching for player profile: " + searchName);
        }
        
        try {
            // 1. Search in /player/dan/ (high reliability)
            String danUrl = "https://www.nihonkiin.or.jp/player/dan/";
            Document danDoc = Jsoup.connect(danUrl).get();
            // 段位一覧ページには「初段」～「九段」のリンクがある
            Elements links = danDoc.select("a[href*=/player/htm/ki]");
            
            String normalizedSearchName = searchName.replaceAll("[\\s\u3000]+", "");
            for (Element link : links) {
                // Match name (remove all whitespace for comparison)
                String linkText = link.text().replaceAll("[\\s\u3000]+", "");

                // Check link text or image alt attribute (for title holders)
                Element img = link.selectFirst("img");
                String altText = (img != null) ? img.attr("alt").replaceAll("[\\s\u3000]+", "") : "";

                if (linkText.equals(normalizedSearchName) || altText.equals(normalizedSearchName) || link.attr("href").contains(normalizedSearchName)) {
                    String detailUrl = link.absUrl("href");
                    scrapePlayerDetail(searchName, detailUrl);
                    return;
                }
            }
            
            // 2. Search in Kansaikiin if not found in Nihonkiin
            if (kansaikiinPlayerScraper.scrapeAndSavePlayer(searchName)) {
                return;
            }
            
            logger.warn("Profile URL not found for player: " + playerName + " (searched as: " + searchName + ")");
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

            // プロフィール見出しからカタカナを抽出 (例: "一力　遼（イチリキ　リョウ）")
            Element nameHeading = doc.selectFirst("h1.player-name, div.player-name h1");
            String kanaName = null;
            Pattern p = Pattern.compile("（([\\u30A0-\\u30FF\\s\u3000]+)[^）]*）");

            if (nameHeading != null) {
                String fullTitle = nameHeading.text();
                Matcher m = p.matcher(fullTitle);
                if (m.find()) {
                    kanaName = m.group(1).trim();
                }
            }

            // 見出しにない場合はページ全体から検索（フォールバック）
            if (kanaName == null) {
                String pageText = doc.text();
                Matcher m = p.matcher(pageText);
                if (m.find()) {
                    kanaName = m.group(1).trim();
                }
            }
            
            if (kanaName != null) {
                player.setKanaName(kanaName);
            }

            // "プロフィール" セクションのテキストから生年月日を探す
            Element profileHeading = doc.selectFirst("h2:contains(プロフィール)");
            if (profileHeading != null) {
                Element next = profileHeading.nextElementSibling();
                while (next != null && !next.tagName().equals("h2")) {
                    String text = next.text();
                    try {
                        Pattern pBirth = Pattern.compile("(\\d+)年.*?(\\d+)月(\\d+)日");
                        Matcher m = pBirth.matcher(text);
                        if (m.find()) {
                            int year = Integer.parseInt(m.group(1));
                            int month = Integer.parseInt(m.group(2));
                            int day = Integer.parseInt(m.group(3));
                            player.setBirthDate(LocalDate.of(year, month, day));
                            break;
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to parse birth date from profile text: " + text, e);
                    }
                    next = next.nextElementSibling();
                }
            }

            // Target multiple possible table classes
            Elements tables = doc.select("table.inter-table, table.table1, table.table-rank");
            for (Element table : tables) {
                Elements rows = table.select("tr");
                for (Element row : rows) {
                    Elements ths = row.select("th");
                    Elements tds = row.select("td");
                    if (ths.isEmpty() || tds.isEmpty()) continue;
                    
                    String th = ths.text().trim();
                    String td = tds.text().trim();

                    if (th.contains("Gender") || th.contains("性別")) {
                        player.setGender(td);
                    } else if (th.contains("Place") || th.contains("出身地")) {
                        player.setBirthPlace(td);
                    } else if (th.contains("Affiliation") || th.contains("所属")) {
                        player.setAffiliation(td);
                    } else if (th.contains("Rank") || th.contains("棋士段位")) {
                        player.setRank(td);
                    } else if (th.contains("Birthday") || th.contains("生年月日")) {
                        try {
                            // "1989年（平成元年）5月24日生" のような形式に対応
                            Pattern pBirth = Pattern.compile("(\\d+)年.*?(\\d+)月(\\d+)日");
                            Matcher m = pBirth.matcher(td);
                            if (m.find()) {
                                int year = Integer.parseInt(m.group(1));
                                int month = Integer.parseInt(m.group(2));
                                int day = Integer.parseInt(m.group(3));
                                player.setBirthDate(LocalDate.of(year, month, day));
                            } else {
                                logger.warn("Birth date format not matched: " + td + " for player: " + playerName);
                            }
                        } catch (Exception e) {
                            logger.warn("Failed to parse birth date: " + td + " for player: " + playerName, e);
                        }
                    }
                }
            }

            // NIHONKIIN uses div.photo img for profile pictures
            Element imgElement = doc.selectFirst("div.photo img, div.player-photo img");
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

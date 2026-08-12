package com.example.robotwebsite.batch;

import com.example.robotwebsite.entity.Player;
import com.example.robotwebsite.service.PlayerService;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
            Player p = existingPlayer.get();
            // 24時間以内に更新されている場合はスキップ（差分更新）
            if (p.getUpdatedAt() != null && p.getUpdatedAt().isAfter(LocalDateTime.now().minusHours(24))) {
                logger.info("Skipping player (recently updated): " + searchName);
                return;
            }
            logger.info("Updating player profile: " + searchName);
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
        scrapePlayerBasicInfo(playerName, url);
        scrapePlayerStats(playerName, url);
    }

    public void scrapePlayerBasicInfo(String playerName, String url) {
        try {
            // 基本情報はJsoupで十分取得可能（高速化のためPlaywrightを使わない選択肢もあるが、
            // 既存ロジックがPlaywright前提なのでまずはPlaywrightで実装し、後で最適化を検討）
            // ただし、要件は「読み取れた時点で保存」なので、独立させる。
            Document doc = Jsoup.connect(url).get();
            
            Player player = playerService.findByName(playerName).orElse(new Player());
            player.setName(playerName);
            player.setProfileUrl(url);

            Element rankElement = doc.selectFirst("div.rank");
            if (rankElement != null) {
                player.setRank(rankElement.text().trim());
            }

            // プロフィール見出しからカタカナを抽出
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
                    } catch (Exception e) {}
                    next = next.nextElementSibling();
                }
            }

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
                            Pattern pBirth = Pattern.compile("(\\d+)年.*?(\\d+)月(\\d+)日");
                            Matcher m = pBirth.matcher(td);
                            if (m.find()) {
                                player.setBirthDate(LocalDate.of(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3))));
                            }
                        } catch (Exception e) {}
                    }
                }
            }

            Element imgElement = doc.selectFirst("div.photo img, div.player-photo img, img[src*=player/photo]");
            if (imgElement != null) {
                player.setIconPath(imgElement.absUrl("src"));
            }

            playerService.saveOrUpdate(player);
            logger.info("Saved basic info for: " + playerName);
        } catch (Exception e) {
            logger.error("Failed to scrape basic info for: " + playerName, e);
        }
    }

    public void scrapePlayerStats(String playerName, String url) {
        Player player = playerService.findByName(playerName).orElse(null);
        if (player == null) {
            logger.warn("Player not found in DB for stats update: " + playerName);
            return;
        }

        // 24時間以内に更新されており、かつ成績データが既に存在する場合はスキップ
        if (player.getUpdatedAt() != null && 
            player.getUpdatedAt().isAfter(LocalDateTime.now().minusHours(24)) &&
            player.getRecentStats() != null) {
            logger.info("Skipping stats update (recently updated and stats exist): " + playerName);
            return;
        }

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();
            page.navigate(url);
            try {
                // 「棋士成績」や「対局結果」のテーブルが表示されるまで待機
                // 10秒の制限を外し、セレクタが出現するまで待つ（Playwrightのデフォルト待機を利用、必要に応じて調整）
                page.waitForSelector("h2:has-text(\"棋士成績\") + table, h2:has-text(\"対局結果と今後の予定\") + table");
            } catch (Exception e) {
                logger.warn("Waiting for stats tables for " + playerName + " failed or timed out.");
            }

            String html = page.content();
            Document doc = Jsoup.parse(html, url);
            browser.close();

            // 棋士成績テーブル
            // 直後のtableだけでなく、セクション内のtableを探すように柔軟にする
            Element statsHeading = doc.selectFirst("h2:contains(棋士成績)");
            Element statsTable = null;
            if (statsHeading != null) {
                statsTable = statsHeading.nextElementSibling();
                while (statsTable != null && !statsTable.tagName().equals("table") && !statsTable.tagName().equals("h2")) {
                    statsTable = statsTable.nextElementSibling();
                }
                if (statsTable != null && !statsTable.tagName().equals("table")) {
                    statsTable = null;
                }
            }

            if (statsTable != null) {
                removeLinks(statsTable);
                statsTable.addClass("table table-sm table-bordered mt-2");
                player.setRecentStats(statsTable.outerHtml());
            } else {
                logger.warn("Could not find stats table for: " + playerName);
            }

            // 対局結果・予定テーブル
            Element scheduleHeading = doc.selectFirst("h2:contains(対局結果と今後の予定)");
            Element scheduleTable = null;
            if (scheduleHeading != null) {
                scheduleTable = scheduleHeading.nextElementSibling();
                while (scheduleTable != null && !scheduleTable.tagName().equals("table") && !scheduleTable.tagName().equals("h2")) {
                    scheduleTable = scheduleTable.nextElementSibling();
                }
                if (scheduleTable != null && !scheduleTable.tagName().equals("table")) {
                    scheduleTable = null;
                }
            }

            if (scheduleTable != null) {
                removeLinks(scheduleTable);
                scheduleTable.addClass("table table-sm table-bordered mt-2");
                player.setRecentMatches(scheduleTable.outerHtml());
            } else {
                logger.warn("Could not find schedule table for: " + playerName);
            }

            playerService.saveOrUpdate(player);
            logger.info("Updated stats and matches for: " + playerName);
        } catch (Exception e) {
            logger.error("Failed to fetch player stats via Playwright: " + url, e);
        }
    }

    private void removeLinks(Element element) {
        Elements links = element.select("a");
        for (Element link : links) {
            link.unwrap();
        }
    }

    public void scrapeAllPlayersBasicInfo() {
        processAllPlayers(this::scrapePlayerBasicInfo);
    }

    public void scrapeAllPlayersStats() {
        java.util.List<Player> players = playerService.findAll();
        for (Player p : players) {
            if (p.getAffiliation() != null && p.getAffiliation().contains("日本棋院") && p.getProfileUrl() != null) {
                scrapePlayerStats(p.getName(), p.getProfileUrl());
            }
        }
    }

    private void processAllPlayers(java.util.function.BiConsumer<String, String> processor) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();
            String listUrl = "https://www.nihonkiin.or.jp/player/kana";
            page.navigate(listUrl);
            try {
                page.waitForSelector("a[href*=/player/htm/ki]", new Page.WaitForSelectorOptions().setTimeout(10000));
            } catch (Exception e) {}
            String html = page.content();
            Document doc = Jsoup.parse(html, listUrl);
            browser.close();

            Elements links = doc.select("a[href*=/player/htm/ki]");
            for (Element link : links) {
                String detailUrl = link.absUrl("href");
                if (detailUrl.contains("/player/htm/ki") && detailUrl.endsWith(".html") && !detailUrl.contains("#")) {
                    String name = link.text().replaceAll("[\\s\u3000]+", "");
                    if (!name.isEmpty()) {
                        processor.accept(name, detailUrl);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error in processAllPlayers", e);
        }
    }

    public void scrapeAllPlayers() {
        scrapeAllPlayersBasicInfo();
        scrapeAllPlayersStats();
    }
}

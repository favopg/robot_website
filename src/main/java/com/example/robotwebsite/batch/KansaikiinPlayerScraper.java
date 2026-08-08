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
public class KansaikiinPlayerScraper {

    private static final Logger logger = LoggerFactory.getLogger(KansaikiinPlayerScraper.class);
    private final PlayerService playerService;
    private static final String LIST_URL = "https://kansaikiin.jp/wp/prokisi/";

    public KansaikiinPlayerScraper(PlayerService playerService) {
        this.playerService = playerService;
    }

    public boolean scrapeAndSavePlayer(String playerName) {
        if (playerName == null || playerName.isEmpty()) return false;

        String searchName = playerService.normalizeName(playerName);
        String normalizedSearchName = searchName.replaceAll("[\\s\u3000]+", "");
        
        Optional<Player> existingPlayer = playerService.findByName(searchName);
        if (existingPlayer.isPresent()) {
            Player p = existingPlayer.get();
            if (p.getGender() != null && p.getRank() != null && p.getBirthDate() != null && p.getKanaName() != null && !p.getKanaName().isEmpty()) {
                return true;
            }
            logger.info("Re-scraping Kansaikiin player profile to fill missing info: " + searchName);
        } else {
            logger.info("Searching for Kansaikiin player profile: " + searchName);
        }

        try {
            Document doc = Jsoup.connect(LIST_URL).get();
            // 名前からリンクを探す（空白を無視して比較）
            
            Elements links = doc.select("a[href*=kisi_prof/]");
            for (Element link : links) {
                String linkText = link.text().replaceAll("[\\s\u3000]+", "");
                if (linkText.equals(normalizedSearchName)) {
                    String detailUrl = link.absUrl("href");
                    scrapePlayerDetail(searchName, detailUrl);
                    return true;
                }
            }
            
            logger.warn("Profile URL not found in Kansaikiin for player: " + playerName + " (searched as: " + searchName + ")");
        } catch (Exception e) {
            logger.error("Error searching Kansaikiin player: " + playerName, e);
        }
        return false;
    }

    public void scrapePlayerDetail(String playerName, String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            Player player = new Player();
            player.setName(playerName);
            player.setProfileUrl(url);
            player.setAffiliation("関西棋院");

            // 段位の抽出
            // h1やタイトル付近にある "氏名 九段" のようなテキストから抽出
            // murakawadaisuke.html の例: <h1>棋士紹介</h1> ... **村川大介 九段**
            Elements strongs = doc.select("strong");
            String rank = null;
            for (Element strong : strongs) {
                String text = strong.text();
                if (text.contains("段")) {
                    Pattern pattern = Pattern.compile("([一二三四五六七八九十]|\\d+)段");
                    Matcher matcher = pattern.matcher(text);
                    if (matcher.find()) {
                        rank = matcher.group();
                        break;
                    }
                }
            }
            player.setRank(rank);

            // 性別の推定（関西棋院のプロフィールに明示的な性別がない場合が多い）
            // 既存の仕組みに合わせるため、必要なら特定キーワードから推測するか、デフォルトをセット
            if (doc.text().contains("女流") || doc.text().contains("娘")) {
                // 文脈から推測するのは危険だが、暫定的に
            }

            // プロフィールテーブルの解析
            Elements tables = doc.select("table");
            for (Element table : tables) {
                Elements rows = table.select("tr");
                for (Element row : rows) {
                    String text = row.text().replaceAll("[\u00a0\u1680\u180e\u2000-\u200a\u202f\u205f\u3000]", " ").trim();
                    if (text.contains("生年月日")) {
                        String dateStr = text.replace("生年月日", "").trim();
                        try {
                            // "1989年（平成元年）5月24日生" のような形式に対応
                            Pattern p = Pattern.compile("(\\d+)年.*?(\\d+)月(\\d+)日");
                            Matcher m = p.matcher(dateStr);
                            if (m.find()) {
                                int year = Integer.parseInt(m.group(1));
                                int month = Integer.parseInt(m.group(2));
                                int day = Integer.parseInt(m.group(3));
                                player.setBirthDate(LocalDate.of(year, month, day));
                            } else {
                                logger.warn("Birth date format not matched: " + dateStr + " for player: " + playerName);
                            }
                        } catch (Exception e) {
                            logger.warn("Failed to parse birth date: " + dateStr + " for player: " + playerName, e);
                        }
                    } else if (text.contains("出　　身")) {
                        String birthPlace = text.replace("出　　身", "").trim();
                        player.setBirthPlace(birthPlace);
                    }
                }
            }
            
            // プロフィール2テーブル目（趣味などがある方）
            for (Element row : doc.select("table tr")) {
                String th = row.select("th").text().trim();
                String td = row.select("td").text().trim();
                if (th.equals("血液型")) {
                    // 血液型もフィールドなし
                }
            }

            // 画像の抽出
            Element imgElement = doc.selectFirst("img[src*=kisi_img/]");
            if (imgElement != null) {
                player.setIconPath(imgElement.absUrl("src"));
                
                // 画像のalt属性に "村川　大介（ムラカワ　ダイスケ）" のように入っている場合がある
                String alt = imgElement.attr("alt");
                String kanaName = null;
                Pattern p = Pattern.compile("（([\\u30A0-\\u30FF\\s\u3000]+)[^）]*）");

                if (alt != null && !alt.isEmpty()) {
                    Matcher m = p.matcher(alt);
                    if (m.find()) {
                        kanaName = m.group(1).trim();
                    }
                }

                // 画像のaltにない場合はページ全体から検索（フォールバック）
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
            }

            playerService.saveOrUpdate(player);
            logger.info("Saved Kansaikiin player info for: " + playerName);

        } catch (IOException e) {
            logger.error("Failed to fetch Kansaikiin player detail page: " + url, e);
        }
    }
}

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
            Elements strongs = doc.select("strong, h1, h2");
            String rank = null;
            for (Element element : strongs) {
                String text = element.text();
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
                for (int i = 0; i < rows.size(); i++) {
                    Element row = rows.get(i);
                    String text = row.text().replaceAll("[\u00a0\u1680\u180e\u2000-\u200a\u202f\u205f\u3000]", " ").trim();
                    if (text.contains("生年月日")) {
                        String dateStr = text.replace("生年月日", "").trim();
                        // もし同じ行に日付がない場合、次の行を確認する
                        if (dateStr.isEmpty() && i + 1 < rows.size()) {
                            dateStr = rows.get(i + 1).text().trim();
                        }
                        try {
                            // 1. "平成7年(1995年)6月19日" のように和暦の後に西暦が括弧書きされている形式を最優先
                            Pattern pWithParentheses = Pattern.compile("[\\(（](\\d+)年[\\)）]");
                            Matcher mWithParentheses = pWithParentheses.matcher(dateStr);
                            
                            int year = -1;
                            if (mWithParentheses.find()) {
                                year = Integer.parseInt(mWithParentheses.group(1));
                            }

                            // 2. 括弧がない場合、あるいは括弧内に年がない場合、"1995年6月19日" のような西暦を探す
                            if (year < 1000) {
                                Pattern pYear = Pattern.compile("(\\d{4})年");
                                Matcher mYear = pYear.matcher(dateStr);
                                if (mYear.find()) {
                                    year = Integer.parseInt(mYear.group(1));
                                }
                            }

                            // 月日の抽出
                            Pattern pMonthDay = Pattern.compile("(\\d+)月(\\d+)日");
                            Matcher mMonthDay = pMonthDay.matcher(dateStr);

                            if (year >= 1000 && mMonthDay.find()) {
                                int month = Integer.parseInt(mMonthDay.group(1));
                                int day = Integer.parseInt(mMonthDay.group(2));
                                player.setBirthDate(LocalDate.of(year, month, day));
                            } else {
                                logger.warn("Could not reliably parse birth date: " + dateStr + " for player: " + playerName);
                            }
                        } catch (Exception e) {
                            logger.warn("Failed to parse birth date: " + dateStr + " for player: " + playerName, e);
                        }
                    } else if (text.replaceAll("[\\s\u3000]+", "").contains("出身")) {
                        String birthPlace = text.replaceAll(".*出身", "").replaceAll("[\\s\u3000]+", "").trim();
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
            Element imgElement = doc.selectFirst("img[src*=kisi_img/], img[src*=kisi_prof/]");
            if (imgElement != null) {
                player.setIconPath(imgElement.absUrl("src"));
                
                // 画像のalt属性に "村川　大介（ムラカワ　ダイスケ）" のように入っている場合がある
                String alt = imgElement.attr("alt");
                String kanaName = null;
                // カタカナとひらがな、および全角スペースを許可するパターン
                Pattern pFull = Pattern.compile("[（(]([\\u30A0-\\u30FF\\u3040-\\u309F\\s\u3000]+)[^）)]*[）)]");

                if (alt != null && !alt.isEmpty()) {
                    Matcher m = pFull.matcher(alt);
                    if (m.find()) {
                        kanaName = m.group(1).trim();
                    }
                }

                // 見出しタグ<h1>や<h2>からも探してみる
                if (kanaName == null) {
                    Elements headers = doc.select("h1, h2");
                    for (Element h : headers) {
                        Matcher m = pFull.matcher(h.text());
                        if (m.find()) {
                            kanaName = m.group(1).trim();
                            break;
                        }
                    }
                }

                if (kanaName != null) {
                    // 指示通り全角スペースを保持する
                    player.setKanaName(kanaName);
                }
            }

            playerService.saveOrUpdate(player);
            logger.info("Saved Kansaikiin player info for: " + playerName + " (Kana: " + player.getKanaName() + ")");

        } catch (IOException e) {
            logger.error("Failed to fetch Kansaikiin player detail page: " + url, e);
        }
    }
    public void scrapeAllPlayers() {
        try {
            Document doc = Jsoup.connect(LIST_URL).get();
            Elements links = doc.select("a[href*=kisi_prof/]");
            for (Element link : links) {
                String name = link.text().replaceAll("[\\s\u3000]+", "");
                String detailUrl = link.absUrl("href");
                if (!name.isEmpty() && detailUrl.contains("prof")) {
                    scrapePlayerDetail(name, detailUrl);
                }
            }
        } catch (Exception e) {
            logger.error("Error scraping Kansaikiin all players", e);
        }
    }
}

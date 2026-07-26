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
import java.time.format.DateTimeFormatter;
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
            // もし最近更新されていたらスキップするなどのロジックを入れることも可能
            // 今回は存在しなければ取得する方針
            return;
        }

        logger.info("Scraping details for player: " + playerName);
        
        try {
            // 日本棋院の検索用URL（名前で検索して詳細ページを探すのは難しいので、
            // 本来はIDが必要だが、ここでは提示されたURLの構造から推測するか、
            // 検索結果から取得する。
            // ただし、提示されたタスクでは「指定されたURL（例：ki000385.html）をJSoupで解析」とある。
            // 実際には対局一覧から詳細へのリンクを取得するのが望ましい。
            // 暫定的に、検索ページから棋士を探すロジックを検討。
            
            // 日本棋院の棋士検索 (名前で検索)
            String searchUrl = "https://www.nihonkiin.or.jp/player/htm/search.php?name=" + playerName;
            // 実際にはもっと複雑な場合があるが、ここでは直接詳細ページを特定する方法を模索。
            // 日本棋院のサイト構成上、名前から直接URLを叩くことはできない。
            // 対局一覧のHTMLに対局者の詳細ページへのリンクが含まれていないか確認が必要。
            
            // NihonkiinMatchScraperTaskletを確認すると、リンクは取得していない。
            // しかし、多くの棋士のURLは /player/htm/kiXXXXXX.html の形式。
            
            // 課題の指示通り、特定のURLを解析するメソッドを作成し、
            // 呼び出し側でURLを特定する方法を考える。
        } catch (Exception e) {
            logger.error("Error scraping player: " + playerName, e);
        }
    }

    public void scrapePlayerDetail(String playerName, String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            Player player = playerRepository.findByName(playerName).orElse(new Player());
            player.setName(playerName);
            player.setProfileUrl(url);

            // 段位の取得
            Element rankElement = doc.selectFirst("div.rank");
            if (rankElement != null) {
                player.setRank(rankElement.text().trim());
            }

            // 性別、生年月日、出身地、門下の取得
            Elements tables = doc.select("table.inter-table");
            for (Element table : tables) {
                Elements rows = table.select("tr");
                for (Element row : rows) {
                    String th = row.select("th").text().trim();
                    String td = row.select("td").text().trim();

                    if (th.contains("性別")) {
                        player.setGender(td);
                    } else if (th.contains("生年月日")) {
                        player.setBirthDate(parseJapaneseDate(td));
                    } else if (th.contains("出身地")) {
                        player.setBirthPlace(td);
                    } else if (th.contains("師匠") || th.contains("門下")) {
                        player.setMaster(td);
                    }
                }
            }
            
            // アイコン画像の取得
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
            // 例: 1989年12月15日
            Pattern pattern = Pattern.compile("(\\d+)年(\\d+)月(\\d+)日");
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

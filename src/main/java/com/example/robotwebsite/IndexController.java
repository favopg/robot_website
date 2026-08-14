package com.example.robotwebsite;

import com.example.robotwebsite.batch.KansaikiinPlayerScraper;
import com.example.robotwebsite.batch.NihonkiinPlayerScraper;
import com.example.robotwebsite.entity.Event;
import com.example.robotwebsite.entity.Match;
import com.example.robotwebsite.entity.YoutubeLive;
import com.example.robotwebsite.repository.EventRepository;
import com.example.robotwebsite.repository.MatchRepository;
import com.example.robotwebsite.repository.ReleaseInfoRepository;
import com.example.robotwebsite.repository.YoutubeLiveRepository;
import com.example.robotwebsite.entity.Player;
import com.example.robotwebsite.service.PlayerService;
import com.example.robotwebsite.service.SystemStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class IndexController {

    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    private final EventRepository eventRepository;
    private final MatchRepository matchRepository;
    private final PlayerService playerService;
    private final YoutubeLiveRepository youtubeLiveRepository;
    private final ReleaseInfoRepository releaseInfoRepository;
    private final NihonkiinPlayerScraper nihonkiinPlayerScraper;
    private final KansaikiinPlayerScraper kansaikiinPlayerScraper;
    private final SystemStatusService systemStatusService;

    public IndexController(EventRepository eventRepository, MatchRepository matchRepository,
                           PlayerService playerService, YoutubeLiveRepository youtubeLiveRepository,
                           ReleaseInfoRepository releaseInfoRepository,
                           NihonkiinPlayerScraper nihonkiinPlayerScraper,
                           KansaikiinPlayerScraper kansaikiinPlayerScraper,
                           SystemStatusService systemStatusService) {
        this.eventRepository = eventRepository;
        this.matchRepository = matchRepository;
        this.playerService = playerService;
        this.youtubeLiveRepository = youtubeLiveRepository;
        this.releaseInfoRepository = releaseInfoRepository;
        this.nihonkiinPlayerScraper = nihonkiinPlayerScraper;
        this.kansaikiinPlayerScraper = kansaikiinPlayerScraper;
        this.systemStatusService = systemStatusService;
    }

    @GetMapping("/api/player/update-kana")
    @ResponseBody
    public Player updatePlayerKana(@RequestParam String name) {
        String normalizedName = playerService.normalizeName(name);
        // 日本棋院を試す
        nihonkiinPlayerScraper.scrapeAndSavePlayer(normalizedName);
        // 関西棋院を試す
        kansaikiinPlayerScraper.scrapeAndSavePlayer(normalizedName);

        return playerService.findByName(normalizedName).orElse(null);
    }

    @GetMapping("/api/players/{name}")
    @ResponseBody
    public Player getPlayerInfo(@PathVariable String name) {
        String normalizedName = playerService.normalizeName(name);
        Player player = playerService.findByName(normalizedName)
                .or(() -> playerService.findByName(name)) // フォールバック: 元の名前でも検索
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));
        
        // アイコンパスの解決
        // まずファイルシステムから検索（優先）
        Set<String> localIcons = getPlayerIcons();
        
        // カタカナ名（スペース除去）での検索を優先
        String kanaForFile = player.getKanaName() != null ? player.getKanaName().replaceAll("[\\s\u3000]+", "") : null;
        if (kanaForFile != null && localIcons.contains(kanaForFile)) {
            player.setIconPath("/images/players/" + kanaForFile + ".jpg");
            return player;
        }

        // 漢字名での検索（フォールバック）
        for (String iconName : localIcons) {
            // 正規化名でアイコンを検索
            if (normalizedName.equals(iconName)) {
                player.setIconPath("/images/players/" + iconName + ".jpg");
                return player;
            }
        }
        
        // ファイルシステムにない場合はDBの値を保持（既にplayerにセットされている）
        return player;
    }

    @PostMapping("/api/players/{name}/like")
    @ResponseBody
    public int likePlayer(@PathVariable String name) {
        return playerService.incrementLikes(name);
    }

    @PostMapping("/api/players/{name}/unlike")
    @ResponseBody
    public int unlikePlayer(@PathVariable String name) {
        return playerService.decrementLikes(name);
    }

    private Set<String> getPlayerIcons() {
        Set<String> iconNames = new HashSet<>();
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            // クラスパス内のリソースをスキャン
            Resource[] resources = resolver.getResources("classpath*:static/images/players/*.jpg");
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (filename != null) {
                    iconNames.add(filename.substring(0, filename.lastIndexOf(".")));
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get player icons", e);
        }
        return iconNames;
    }

    private void setIcons(List<Match> matches) {
        Set<String> icons = getPlayerIcons();
        for (Match m : matches) {
            updatePlayerIcon(m, 1, icons);
            updatePlayerIcon(m, 2, icons);
        }
    }

    private void updatePlayerIcon(Match m, int playerNum, Set<String> icons) {
        String originalName = (playerNum == 1) ? m.getPlayer1Name() : m.getPlayer2Name();
        if (originalName == null) return;

        String name = playerService.normalizeName(originalName);

        // Player情報を取得して生年月日と性別を設定
        Optional<Player> playerOpt = playerService.findByName(name);
        playerOpt.ifPresent(p -> {
            if (playerNum == 1) {
                m.setPlayer1BirthDate(p.getBirthDate());
                m.setPlayer1Gender(p.getGender());
                m.setPlayer1Kana(p.getKanaName());
            } else {
                m.setPlayer2BirthDate(p.getBirthDate());
                m.setPlayer2Gender(p.getGender());
                m.setPlayer2Kana(p.getKanaName());
            }
        });

        // まずファイルシステムから検索（優先）
        String iconPath = null;
        
        // カタカナ名での検索を優先
        if (playerOpt.isPresent()) {
            Player p = playerOpt.get();
            if (p.getKanaName() != null) {
                String kanaForFile = p.getKanaName().replaceAll("[\\s\u3000]+", "");
                if (icons.contains(kanaForFile)) {
                    iconPath = "/images/players/" + kanaForFile + ".jpg";
                }
            }
        }

        // 漢字名での検索（フォールバック）
        if (iconPath == null) {
            if (icons.contains(name)) {
                iconPath = "/images/players/" + name + ".jpg";
            }
        }

        // ファイルシステムにない場合はDBから検索
        if (iconPath == null) {
            iconPath = playerOpt
                    .map(Player::getIconPath)
                    .filter(path -> !path.isEmpty())
                    .orElse(null);
        }

        if (playerNum == 1) {
            m.setPlayer1Icon(iconPath);
        } else {
            m.setPlayer2Icon(iconPath);
        }
    }

    @GetMapping("/")
    public String index(Model model) {
        // LP用に最新の5件のみ取得
        Pageable pageable = PageRequest.of(0, 5, Sort.by("eventDate").descending());
        Page<Event> eventPage = eventRepository.findAll(pageable);
        model.addAttribute("events", eventPage.getContent());
        model.addAttribute("isUpdating", systemStatusService.isUpdating());

        // リリース情報取得
        model.addAttribute("releaseInfos", releaseInfoRepository.findTop5ByOrderByCreatedAtDesc());
        model.addAttribute("totalReleaseCount", releaseInfoRepository.count());

        return "index";
    }

    @GetMapping("/ranking")
    public String ranking(Model model) {
        List<Player> players = playerService.getPopularPlayers();
        // アイコン設定
        Set<String> icons = getPlayerIcons();
        for (Player p : players) {
            String name = playerService.normalizeName(p.getName());
            String iconPath = null;
            
            // カタカナ名での検索を優先
            if (p.getKanaName() != null) {
                String kanaForFile = p.getKanaName().replaceAll("[\\s\u3000]+", "");
                if (icons.contains(kanaForFile)) {
                    iconPath = "/images/players/" + kanaForFile + ".jpg";
                }
            }
            
            // 漢字名での検索（フォールバック）
            if (iconPath == null) {
                if (icons.contains(name)) {
                    iconPath = "/images/players/" + name + ".jpg";
                }
            }
            
            // DBに保存されているパスがあればそれを使う（もしあれば）
            if (iconPath == null && p.getIconPath() != null && !p.getIconPath().isEmpty()) {
                iconPath = p.getIconPath();
            }
            
            p.setIconPath(iconPath);
        }
        model.addAttribute("players", players);
        return "ranking";
    }

    @GetMapping("/events")
    public String allEvents(Model model, @RequestParam(defaultValue = "0") int page) {
        // 全件表示用のページング（1ページ20件）
        Pageable pageable = PageRequest.of(page, 20, Sort.by("eventDate").descending());
        Page<Event> eventPage = eventRepository.findAll(pageable);
        model.addAttribute("events", eventPage);
        return "event_list";
    }

    @GetMapping("/youtube-schedule")
    public String youtubeSchedule(Model model) {
        // YouTube配信予定を取得
        List<YoutubeLive> youtubeLives = youtubeLiveRepository.findAll(Sort.by(Sort.Direction.ASC, "scheduledStartTime"));
        // 時間情報がないものもあるため、全件取得してリスト表示
        if (youtubeLives.isEmpty()) {
            youtubeLives = youtubeLiveRepository.findAll();
        }
        model.addAttribute("youtubeLives", youtubeLives);
        model.addAttribute("title", "YouTube 配信予定（日本棋院）");
        return "youtube_list";
    }

    @GetMapping("/match-results")
    public String matchResults(Model model,
                               @RequestParam(required = false) Integer year,
                               @RequestParam(required = false) Integer month,
                               @RequestParam(required = false, defaultValue = "false") boolean all) {
        List<Match> results;
        String titlePrefix = "";

        if (all) {
            List<Match> allMatches = matchRepository.findAll(Sort.by(Sort.Direction.DESC, "matchDate"));
            results = allMatches.stream()
                    .filter(m -> m.getResult() != null && !m.getResult().isEmpty())
                    .collect(Collectors.toList());
            titlePrefix = "全データ ";
        } else if (year != null && month != null) {
            LocalDate startDate = LocalDate.of(year, month, 1);
            titlePrefix = year + "年" + month + "月 ";
            List<Match> allMatches = matchRepository.findByMatchDateBetweenOrderByMatchDateDesc(
                    startDate, startDate.withDayOfMonth(startDate.lengthOfMonth()));
            results = allMatches.stream()
                    .filter(m -> m.getResult() != null && !m.getResult().isEmpty())
                    .collect(Collectors.toList());
        } else {
            LocalDate today = LocalDate.now();
            List<Match> allMatches = matchRepository.findByMatchDateBetweenOrderByMatchDateDesc(
                    today.minusWeeks(2), today);
            results = allMatches.stream()
                    .filter(m -> m.getResult() != null && !m.getResult().isEmpty())
                    .collect(Collectors.toList());
        }

        // 月別リンク用のデータを生成（データがある月のみ）
        LocalDate now = LocalDate.now();
        List<String> months = new java.util.ArrayList<>();
        for (int i = 0; i < 12; i++) {
            LocalDate d = now.minusMonths(i);
            LocalDate start = d.withDayOfMonth(1);
            LocalDate end = d.withDayOfMonth(d.lengthOfMonth());
            
            List<Match> monthlyMatches = matchRepository.findByMatchDateBetweenOrderByMatchDateDesc(start, end);
            boolean hasResults = monthlyMatches.stream()
                    .anyMatch(m -> m.getResult() != null && !m.getResult().isEmpty());
            
            if (hasResults) {
                months.add(d.getYear() + "-" + d.getMonthValue());
            }
        }

        model.addAttribute("matches", results);
        setIcons(results);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("months", months);
        model.addAttribute("title", titlePrefix + "プロ棋士対局結果");
        model.addAttribute("isUpdating", systemStatusService.isUpdating());
        return "match_list";
    }

    @GetMapping("/match-schedule")
    public String matchSchedule(Model model) {
        LocalDate today = LocalDate.now();
        // 今後の予定を取得
        List<Match> allMatches = matchRepository.findByMatchDateBetweenOrderByMatchDateAsc(
                today, today.plusWeeks(2));

        List<Match> schedules = allMatches.stream()
                .filter(m -> m.getResult() == null || m.getResult().isEmpty())
                .collect(Collectors.toList());

        model.addAttribute("matches", schedules);
        setIcons(schedules);
        model.addAttribute("today", today);
        model.addAttribute("title", "プロ棋士対局予定");
        model.addAttribute("isUpdating", systemStatusService.isUpdating());
        return "match_list";
    }


    @GetMapping("/honinbo-info")
    public String honinboInfo(Model model) {
        model.addAttribute("title", "本因坊戦 詳細情報");
        return "honinbo_info";
    }

    @GetMapping("/kisei-info")
    public String kiseiInfo(Model model) {
        model.addAttribute("title", "棋聖戦 詳細情報");
        return "kisei_info";
    }

    @GetMapping("/meijin-info")
    public String meijinInfo(Model model) {
        model.addAttribute("title", "名人戦 詳細情報");
        return "meijin_info";
    }

    @GetMapping("/ouza-info")
    public String ouzaInfo(Model model) {
        model.addAttribute("title", "王座戦 詳細情報");
        return "ouza_info";
    }

    @GetMapping("/tengen-info")
    public String tengenInfo(Model model) {
        model.addAttribute("title", "天元戦 詳細情報");
        return "tengen_info";
    }

    @GetMapping("/gosei-info")
    public String goseiInfo(Model model) {
        model.addAttribute("title", "碁聖戦 詳細情報");
        return "gosei_info";
    }

    @GetMapping("/judan-info")
    public String judanInfo(Model model) {
        model.addAttribute("title", "十段戦 詳細情報");
        return "judan_info";
    }

    @GetMapping("/female-honinbo-info")
    public String femaleHoninboInfo(Model model) {
        model.addAttribute("title", "女流本因坊戦 詳細情報");
        return "female_honinbo_info";
    }

    @GetMapping("/female-meijin-info")
    public String femaleMeijinInfo(Model model) {
        model.addAttribute("title", "女流名人戦 詳細情報");
        return "female_meijin_info";
    }

    @GetMapping("/female-kisei-info")
    public String femaleKiseiInfo(Model model) {
        model.addAttribute("title", "女流棋聖戦 詳細情報");
        return "female_kisei_info";
    }

    @GetMapping("/senko-info")
    public String senkoInfo(Model model) {
        model.addAttribute("title", "扇興杯 詳細情報");
        return "senko_info";
    }

    @GetMapping("/nhk-info")
    public String nhkInfo(Model model) {
        model.addAttribute("title", "NHK杯 詳細情報");
        return "nhk_info";
    }
}

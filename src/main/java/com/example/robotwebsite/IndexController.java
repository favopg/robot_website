package com.example.robotwebsite;

import com.example.robotwebsite.entity.Event;
import com.example.robotwebsite.entity.Match;
import com.example.robotwebsite.entity.YoutubeLive;
import com.example.robotwebsite.repository.EventRepository;
import com.example.robotwebsite.repository.MatchRepository;
import com.example.robotwebsite.repository.YoutubeLiveRepository;
import com.example.robotwebsite.entity.Player;
import com.example.robotwebsite.service.PlayerService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class IndexController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private YoutubeLiveRepository youtubeLiveRepository;

    @GetMapping("/api/players/{name}")
    @ResponseBody
    public Player getPlayerInfo(@PathVariable String name) {
        Player player = playerService.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));
        
        // アイコンパスの解決（対局一覧と同じロジックを適用）
        // まずファイルシステムから検索（優先）
        Set<String> localIcons = getPlayerIcons();
        for (String iconName : localIcons) {
            if (name.contains(iconName) || iconName.contains(name)) {
                player.setIconPath("/images/players/" + iconName + ".jpg");
                return player;
            }
        }
        
        // ファイルシステムにない場合はDBの値を保持（既にplayerにセットされている）
        return player;
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
            e.printStackTrace();
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
        String name = (playerNum == 1) ? m.getPlayer1Name() : m.getPlayer2Name();
        if (name == null) return;

        // まずファイルシステムから検索（優先）
        String iconPath = null;
        for (String iconName : icons) {
            if (name.contains(iconName) || iconName.contains(name)) {
                iconPath = "/images/players/" + iconName + ".jpg";
                break;
            }
        }

        // ファイルシステムにない場合はDBから検索
        if (iconPath == null) {
            iconPath = playerService.findByName(name)
                    .map(Player::getIconPath)
                    .filter(path -> path != null && !path.isEmpty())
                    .orElse(null);
        }

        if (playerNum == 1) {
            m.setPlayer1Icon(iconPath);
        } else {
            m.setPlayer2Icon(iconPath);
        }
    }

    @GetMapping("/")
    public String index(Model model, @RequestParam(defaultValue = "0") int page) {
        // 既存のイベント情報をページング表示（1ページ20件）
        Pageable pageable = PageRequest.of(page, 20, Sort.by("eventDate").descending());
        Page<Event> eventPage = eventRepository.findAll(pageable);
        model.addAttribute("events", eventPage);

        return "index";
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
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
            titlePrefix = year + "年" + month + "月 ";
            List<Match> allMatches = matchRepository.findByMatchDateBetweenOrderByMatchDateDesc(startDate, endDate);
            results = allMatches.stream()
                    .filter(m -> m.getResult() != null && !m.getResult().isEmpty())
                    .collect(Collectors.toList());
        } else {
            LocalDate today = LocalDate.now();
            LocalDate startDate = today.minusWeeks(2);
            LocalDate endDate = today;
            List<Match> allMatches = matchRepository.findByMatchDateBetweenOrderByMatchDateDesc(startDate, endDate);
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
        return "match_list";
    }

    @GetMapping("/match-list")
    public String matchList(Model model) {
        LocalDate today = LocalDate.now();
        // 過去2週間から未来2週間分を取得
        List<Match> allMatches = matchRepository.findByMatchDateBetweenOrderByMatchDateAsc(
                today.minusWeeks(2), today.plusWeeks(2));

        model.addAttribute("matches", allMatches);
        setIcons(allMatches);
        model.addAttribute("today", today);
        model.addAttribute("title", "プロ棋士対局一覧");
        return "match_list";
    }
}

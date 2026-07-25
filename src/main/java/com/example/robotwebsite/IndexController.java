package com.example.robotwebsite;

import com.example.robotwebsite.entity.Event;
import com.example.robotwebsite.entity.Match;
import com.example.robotwebsite.entity.YoutubeLive;
import com.example.robotwebsite.repository.EventRepository;
import com.example.robotwebsite.repository.MatchRepository;
import com.example.robotwebsite.repository.YoutubeLiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
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
    private YoutubeLiveRepository youtubeLiveRepository;

    private Set<String> getPlayerIcons() {
        Set<String> iconNames = new HashSet<>();
        try {
            // クラスパス上のリソースディレクトリから取得
            File folder = new File("src/main/resources/static/images/players");
            if (folder.exists() && folder.isDirectory()) {
                File[] files = folder.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isFile() && f.getName().endsWith(".jpg")) {
                            iconNames.add(f.getName().substring(0, f.getName().lastIndexOf(".")));
                        }
                    }
                }
            }
        } catch (Exception e) {
            // エラー時はログ出力するか、空セットを返す
            e.printStackTrace();
        }
        return iconNames;
    }

    private void setIcons(List<Match> matches) {
        Set<String> icons = getPlayerIcons();
        for (Match m : matches) {
            String p1Name = m.getPlayer1Name();
            String p2Name = m.getPlayer2Name();

            for (String iconName : icons) {
                if (p1Name != null && (p1Name.contains(iconName) || iconName.contains(p1Name))) {
                    m.setPlayer1Icon("/images/players/" + iconName + ".jpg");
                    break;
                }
            }
            for (String iconName : icons) {
                if (p2Name != null && (p2Name.contains(iconName) || iconName.contains(p2Name))) {
                    m.setPlayer2Icon("/images/players/" + iconName + ".jpg");
                    break;
                }
            }
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
        model.addAttribute("months", months);
        model.addAttribute("title", titlePrefix + "プロ棋士対局結果");
        return "match_list";
    }

    @GetMapping("/match-schedule")
    public String matchSchedule(Model model) {
        LocalDate today = LocalDate.now();
        // 今後の予定を取得
        List<Match> allMatches = matchRepository.findByMatchDateBetweenOrderByMatchDateDesc(
                today, today.plusWeeks(2));

        List<Match> schedules = allMatches.stream()
                .filter(m -> m.getResult() == null || m.getResult().isEmpty())
                .collect(Collectors.toList());

        model.addAttribute("matches", schedules);
        setIcons(schedules);
        model.addAttribute("title", "プロ棋士対局予定");
        return "match_list";
    }
}

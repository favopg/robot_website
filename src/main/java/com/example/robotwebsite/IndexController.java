package com.example.robotwebsite;

import com.example.robotwebsite.entity.Event;
import com.example.robotwebsite.entity.Match;
import com.example.robotwebsite.repository.EventRepository;
import com.example.robotwebsite.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class IndexController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private MatchRepository matchRepository;

    @GetMapping("/")
    public String index(Model model) {
        // 既存のイベント情報
        List<Event> events = eventRepository.findAllByOrderByEventDateAsc();
        model.addAttribute("events", events);

        // 対局情報の取得（直近1週間など）
        LocalDate today = LocalDate.now();
        List<Match> allMatches = matchRepository.findByMatchDateBetweenOrderByMatchDateAsc(
                today.minusWeeks(1), today.plusWeeks(1));

        // 「結果があるもの」と「結果がないもの（予定）」に分ける
        List<Match> results = allMatches.stream()
                .filter(m -> m.getResult() != null && !m.getResult().isEmpty())
                .collect(Collectors.toList());

        List<Match> schedules = allMatches.stream()
                .filter(m -> m.getResult() == null || m.getResult().isEmpty())
                .collect(Collectors.toList());

        model.addAttribute("matchResults", results);
        model.addAttribute("matchSchedules", schedules);

        return "index";
    }

    @GetMapping("/match-results")
    public String matchResults(Model model) {
        LocalDate today = LocalDate.now();
        // 直近2週間などの結果を取得
        List<Match> allMatches = matchRepository.findByMatchDateBetweenOrderByMatchDateAsc(
                today.minusWeeks(2), today);

        List<Match> results = allMatches.stream()
                .filter(m -> m.getResult() != null && !m.getResult().isEmpty())
                .collect(Collectors.toList());

        model.addAttribute("matches", results);
        model.addAttribute("title", "プロ棋士対局結果");
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
        model.addAttribute("title", "プロ棋士対局予定");
        return "match_list";
    }
}

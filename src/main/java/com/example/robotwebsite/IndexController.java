package com.example.robotwebsite;

import com.example.robotwebsite.entity.Event;
import com.example.robotwebsite.entity.Match;
import com.example.robotwebsite.repository.EventRepository;
import com.example.robotwebsite.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String index(Model model, @RequestParam(defaultValue = "0") int page) {
        // 既存のイベント情報をページング表示（1ページ20件）
        Pageable pageable = PageRequest.of(page, 20, Sort.by("eventDate").descending());
        Page<Event> eventPage = eventRepository.findAll(pageable);
        model.addAttribute("events", eventPage);

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

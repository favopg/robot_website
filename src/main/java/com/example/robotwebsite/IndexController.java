package com.example.robotwebsite;

import com.example.robotwebsite.entity.Event;
import com.example.robotwebsite.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class IndexController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private com.example.robotwebsite.repository.MatchRepository matchRepository;

    @GetMapping("/")
    public String index(Model model) {
        List<Event> events = eventRepository.findAllByOrderByEventDateAsc();
        model.addAttribute("events", events);
        return "index";
    }

    @GetMapping("/match-schedule")
    public String matchSchedule(Model model) {
        java.time.LocalDate today = java.time.LocalDate.now();
        List<com.example.robotwebsite.entity.Match> matches = matchRepository.findByMatchDateBetweenOrderByMatchDateAsc(
            today.minusWeeks(1), today.plusWeeks(1));
        model.addAttribute("matches", matches);
        return "match_schedule";
    }
}

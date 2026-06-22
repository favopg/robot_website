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

    @GetMapping("/")
    public String index(Model model) {
        List<Event> events = eventRepository.findAllByOrderByEventDateDesc();
        model.addAttribute("events", events);
        return "index";
    }
}

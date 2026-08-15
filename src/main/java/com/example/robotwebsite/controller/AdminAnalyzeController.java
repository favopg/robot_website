package com.example.robotwebsite.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/analyze")
public class AdminAnalyzeController {

    @GetMapping
    public String index() {
        return "admin/analyze";
    }
}

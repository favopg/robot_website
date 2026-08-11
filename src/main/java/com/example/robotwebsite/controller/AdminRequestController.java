package com.example.robotwebsite.controller;

import com.example.robotwebsite.entity.Request;
import com.example.robotwebsite.repository.RequestRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminRequestController {

    private final RequestRepository requestRepository;

    public AdminRequestController(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @GetMapping
    public String index() {
        return "redirect:/admin/request";
    }

    @GetMapping("/request")
    public String list(Model model) {
        model.addAttribute("requests", requestRepository.findAll());
        return "admin/request_list";
    }

    @PostMapping("/request/update-status")
    public String updateStatus(@RequestParam Long id, @RequestParam String status) {
        Request request = requestRepository.findById(id).orElseThrow();
        request.setStatus(status);
        requestRepository.save(request);
        return "redirect:/admin/request";
    }
}

package com.example.robotwebsite.controller;

import com.example.robotwebsite.entity.Inquiry;
import com.example.robotwebsite.repository.InquiryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/inquiry")
public class AdminInquiryController {

    private final InquiryRepository inquiryRepository;

    public AdminInquiryController(InquiryRepository inquiryRepository) {
        this.inquiryRepository = inquiryRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("inquiries", inquiryRepository.findAll());
        return "admin/inquiry_list";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam Long id, @RequestParam String status) {
        Inquiry inquiry = inquiryRepository.findById(id).orElseThrow();
        inquiry.setStatus(status);
        inquiryRepository.save(inquiry);
        return "redirect:/admin/inquiry";
    }
}

package com.example.robotwebsite.controller;

import com.example.robotwebsite.entity.ReleaseInfo;
import com.example.robotwebsite.repository.InquiryRepository;
import com.example.robotwebsite.repository.ReleaseInfoRepository;
import com.example.robotwebsite.repository.RequestRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/release")
public class ReleaseInfoController {

    private final ReleaseInfoRepository releaseInfoRepository;
    private final InquiryRepository inquiryRepository;
    private final RequestRepository requestRepository;

    public ReleaseInfoController(ReleaseInfoRepository releaseInfoRepository,
                                 InquiryRepository inquiryRepository,
                                 RequestRepository requestRepository) {
        this.releaseInfoRepository = releaseInfoRepository;
        this.inquiryRepository = inquiryRepository;
        this.requestRepository = requestRepository;
    }

    @GetMapping
    public String showReleaseForm(Model model) {
        if (!model.containsAttribute("releaseInfo")) {
            model.addAttribute("releaseInfo", new ReleaseInfo());
        }

        List<String> subjects = new ArrayList<>();
        subjects.addAll(requestRepository.findAll().stream()
                .map(r -> "[要望] " + r.getSubject())
                .collect(Collectors.toList()));
        subjects.addAll(inquiryRepository.findAll().stream()
                .map(i -> "[バグ] " + i.getSubject())
                .collect(Collectors.toList()));

        model.addAttribute("subjects", subjects);
        return "admin/release_form";
    }

    @PostMapping("/submit")
    public String submitReleaseInfo(@ModelAttribute ReleaseInfo releaseInfo, RedirectAttributes redirectAttributes) {
        try {
            releaseInfoRepository.save(releaseInfo);
            redirectAttributes.addFlashAttribute("message", "リリース情報を登録しました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "登録に失敗しました: " + e.getMessage());
        }
        return "redirect:/admin/release";
    }
}

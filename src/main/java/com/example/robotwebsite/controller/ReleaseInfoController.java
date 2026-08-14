package com.example.robotwebsite.controller;

import com.example.robotwebsite.entity.ReleaseInfo;
import com.example.robotwebsite.repository.InquiryRepository;
import com.example.robotwebsite.repository.ReleaseInfoRepository;
import com.example.robotwebsite.repository.RequestRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
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

    @GetMapping("/admin/release")
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

    @PostMapping("/admin/release/submit")
    public String submitReleaseInfo(@RequestParam(required = false) String subjectSelect,
                                   @RequestParam(required = false) String subjectInput,
                                   @RequestParam String comment,
                                   RedirectAttributes redirectAttributes) {
        try {
            String subject = (subjectInput != null && !subjectInput.trim().isEmpty()) ? subjectInput : subjectSelect;
            
            if (subject == null || subject.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "件名を選択するか手入力してください。");
                return "redirect:/admin/release";
            }

            ReleaseInfo releaseInfo = new ReleaseInfo();
            releaseInfo.setSubject(subject);
            releaseInfo.setComment(comment);
            
            releaseInfoRepository.save(releaseInfo);
            redirectAttributes.addFlashAttribute("message", "リリース情報を登録しました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "登録に失敗しました: " + e.getMessage());
        }
        return "redirect:/admin/release";
    }

    @GetMapping("/release/list")
    public String listReleaseInfos(Model model) {
        model.addAttribute("releaseInfos", releaseInfoRepository.findAllByOrderByCreatedAtDesc());
        return "release_list";
    }

    @GetMapping("/release/{id}")
    public String showReleaseDetail(@PathVariable Long id, Model model) {
        ReleaseInfo releaseInfo = releaseInfoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Release info not found"));
        model.addAttribute("releaseInfo", releaseInfo);
        return "release_detail";
    }
}

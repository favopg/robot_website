package com.example.robotwebsite;

import com.example.robotwebsite.entity.Inquiry;
import com.example.robotwebsite.repository.InquiryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/inquiry")
public class InquiryController {

    private final InquiryRepository inquiryRepository;

    public InquiryController(InquiryRepository inquiryRepository) {
        this.inquiryRepository = inquiryRepository;
    }

    @GetMapping
    public String showForm(Model model) {
        if (!model.containsAttribute("inquiry")) {
            model.addAttribute("inquiry", new Inquiry());
        }
        return "inquiry_form";
    }

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("inquiries", inquiryRepository.findAll());
        return "inquiry_list";
    }

    @PostMapping("/submit")
    public String submit(@ModelAttribute Inquiry inquiry, RedirectAttributes ra) {
        long count = inquiryRepository.count();
        if (count >= 10) {
            ra.addFlashAttribute("error", "バグ報告制限（合計10件）に達したため、送信できませんでした。");
            return "redirect:/inquiry";
        }

        inquiryRepository.save(inquiry);
        ra.addFlashAttribute("message", "報告ありがとうございました。内容は確認させていただきます。");
        return "redirect:/inquiry";
    }
}

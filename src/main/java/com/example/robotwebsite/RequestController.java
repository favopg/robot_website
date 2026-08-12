package com.example.robotwebsite;

import com.example.robotwebsite.entity.Request;
import com.example.robotwebsite.repository.RequestRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/request")
public class RequestController {

    private final RequestRepository requestRepository;

    public RequestController(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @GetMapping
    public String showForm(Model model) {
        if (!model.containsAttribute("request")) {
            model.addAttribute("request", new Request());
        }
        return "request_form";
    }

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("requests", requestRepository.findAll());
        return "request_list";
    }

    @PostMapping("/submit")
    public String submit(@ModelAttribute Request request, RedirectAttributes ra) {
        long count = requestRepository.count();
        if (count >= 10) {
            ra.addFlashAttribute("error", "要望投稿制限（合計10件）に達したため、送信できませんでした。");
            return "redirect:/request";
        }

        requestRepository.save(request);
        ra.addFlashAttribute("message", "要望ありがとうございました。内容は確認させていただきます。");
        return "redirect:/request";
    }
}

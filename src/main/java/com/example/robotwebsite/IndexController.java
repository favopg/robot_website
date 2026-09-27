package com.example.robotwebsite;

import com.example.robotwebsite.dto.KifuInfo;
import com.example.robotwebsite.service.KatagoAnalyzeService;
import com.example.robotwebsite.service.SystemStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.Collator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class IndexController {

    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    private final SystemStatusService systemStatusService;
    private final KatagoAnalyzeService katagoAnalyzeService;

    public IndexController(SystemStatusService systemStatusService,
                           KatagoAnalyzeService katagoAnalyzeService) {
        this.systemStatusService = systemStatusService;
        this.katagoAnalyzeService = katagoAnalyzeService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("isUpdating", systemStatusService.isUpdating());
        return "index";
    }

    @GetMapping({"/kifu-list", "/kifu"})
    public String kifuList(@RequestParam(value = "player", required = false) String player,
                           @RequestParam(value = "playerName", required = false) String playerName,
                           @RequestParam(value = "search", required = false) String search,
                           @RequestParam(value = "q", required = false) String q,
                           Model model) {
        String query = player;
        if (query == null || query.isBlank()) query = playerName;
        if (query == null || query.isBlank()) query = search;
        if (query == null || query.isBlank()) query = q;

        List<KifuInfo> allKifuList = katagoAnalyzeService.getAvailableKifuList();
        List<KifuInfo> kifuList = allKifuList;

        Collator collator = Collator.getInstance(Locale.JAPANESE);
        List<String> playerList = allKifuList.stream()
                .flatMap(k -> Stream.of(k.getBlackPlayer(), k.getWhitePlayer()))
                .filter(p -> p != null && !p.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(collator::compare)
                .collect(Collectors.toList());

        if (query != null && !query.isBlank()) {
            String trimmedQuery = query.trim();
            String cleanQuery = trimmedQuery.replaceAll("[\\s\u3000]+", "").toLowerCase();
            kifuList = allKifuList.stream()
                    .filter(k -> {
                        String black = k.getBlackPlayer() != null ? k.getBlackPlayer().replaceAll("[\\s\u3000]+", "").toLowerCase() : "";
                        String white = k.getWhitePlayer() != null ? k.getWhitePlayer().replaceAll("[\\s\u3000]+", "").toLowerCase() : "";
                        String match = k.getMatchName() != null ? k.getMatchName().replaceAll("[\\s\u3000]+", "").toLowerCase() : "";
                        return black.contains(cleanQuery) || white.contains(cleanQuery) || match.contains(cleanQuery);
                    })
                    .collect(Collectors.toList());
            model.addAttribute("searchPlayer", trimmedQuery);
        } else {
            model.addAttribute("searchPlayer", "");
        }

        model.addAttribute("playerList", playerList);
        model.addAttribute("kifuList", kifuList);
        model.addAttribute("totalCount", allKifuList.size());
        model.addAttribute("title", "棋譜一覧");
        model.addAttribute("isUpdating", systemStatusService.isUpdating());
        return "kifu_list";
    }

    @GetMapping({"/recommended-kifu", "/recommend-kifu"})
    public String recommendedKifu(Model model) {
        model.addAttribute("title", "おすすめ棋譜");
        return "recommended_kifu";
    }
}

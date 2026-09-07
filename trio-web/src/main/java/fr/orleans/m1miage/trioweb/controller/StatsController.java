package fr.orleans.m1miage.trioweb.controller;

import fr.orleans.m1miage.trioweb.service.StatsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/stats")
    public String stats(Model model) {
        model.addAttribute("stats", statsService.getMyStats());
        return "stats/index";
    }
}

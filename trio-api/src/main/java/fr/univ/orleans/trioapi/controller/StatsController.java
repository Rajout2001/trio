package fr.univ.orleans.trioapi.controller;

import fr.univ.orleans.trioapi.dto.stats.StatistiquesJoueurResponse;
import fr.univ.orleans.trioapi.service.PartieService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatsController {

    private final PartieService partieService;

    public StatsController(PartieService partieService) {
        this.partieService = partieService;
    }

    @GetMapping("/api/stats/me")
    @SecurityRequirement(name = "bearerAuth")
    public StatistiquesJoueurResponse getMesStatistiques(Authentication authentication) {
        return partieService.getStatistiquesJoueur(authentication.getName());
    }
}

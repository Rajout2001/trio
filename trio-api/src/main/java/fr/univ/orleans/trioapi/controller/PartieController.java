package fr.univ.orleans.trioapi.controller;

import fr.univ.orleans.trioapi.dto.partie.CreatePartieRequest;
import fr.univ.orleans.trioapi.dto.partie.DistributionJoueurResponse;
import fr.univ.orleans.trioapi.dto.partie.JoinPartieRequest;
import fr.univ.orleans.trioapi.dto.partie.MoveResultResponse;
import fr.univ.orleans.trioapi.dto.partie.PartieResponse;
import fr.univ.orleans.trioapi.dto.partie.PlayMoveRequest;
import fr.univ.orleans.trioapi.service.PartieService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class PartieController {

    private final PartieService partieService;

    public PartieController(PartieService partieService) {
        this.partieService = partieService;
    }

    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
            "service", "trio-api",
            "status", "ok",
            "endpoints", List.of("/api/parties", "/api/parties/{id}/join", "/api/parties/{id}/start", "/api/parties/{id}/moves")
        );
    }

    @GetMapping("/api/parties")
    @SecurityRequirement(name = "bearerAuth")
    public List<PartieResponse> getParties() {
        return partieService.listerParties();
    }

    @GetMapping("/api/parties/lobby")
    @SecurityRequirement(name = "bearerAuth")
    public List<PartieResponse> getPartiesPourLobby(Authentication authentication) {
        return partieService.listerPartiesPourLobby(authentication.getName());
    }

    @GetMapping("/api/parties/terminees")
    @SecurityRequirement(name = "bearerAuth")
    public List<PartieResponse> getPartiesTerminees() {
        return partieService.listerPartiesTerminees();
    }

    @GetMapping("/api/parties/{partieId}")
    @SecurityRequirement(name = "bearerAuth")
    public PartieResponse getPartie(@PathVariable Long partieId) {
        return partieService.getPartie(partieId);
    }

    @PostMapping("/api/parties")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    public PartieResponse createPartie(@Valid @RequestBody CreatePartieRequest request, Authentication authentication) {
        return partieService.creerPartie(request, authentication.getName());
    }

    @PostMapping("/api/parties/{partieId}/join")
    @SecurityRequirement(name = "bearerAuth")
    public PartieResponse rejoindrePartie(
        @PathVariable Long partieId,
        @RequestBody(required = false) JoinPartieRequest request,
        Authentication authentication
    ) {
        return partieService.rejoindrePartie(partieId, authentication.getName(), request);
    }

    @PostMapping("/api/parties/{partieId}/start")
    @SecurityRequirement(name = "bearerAuth")
    public PartieResponse lancerPartie(@PathVariable Long partieId, Authentication authentication) {
        return partieService.lancerPartie(partieId, authentication.getName());
    }

    @GetMapping("/api/parties/{partieId}/distribution/me")
    @SecurityRequirement(name = "bearerAuth")
    public DistributionJoueurResponse getMaDistribution(@PathVariable Long partieId, Authentication authentication) {
        return partieService.consulterDistribution(partieId, authentication.getName());
    }

    @PostMapping("/api/parties/{partieId}/moves")
    @SecurityRequirement(name = "bearerAuth")
    public MoveResultResponse jouerCoup(
        @PathVariable Long partieId,
        @Valid @RequestBody PlayMoveRequest request,
        Authentication authentication
    ) {
        return partieService.jouerCoup(partieId, authentication.getName(), request);
    }
}

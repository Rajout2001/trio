package fr.orleans.m1miage.trioweb.controller;

import fr.orleans.m1miage.trioweb.dtos.CreateGameForm;
import fr.orleans.m1miage.trioweb.dtos.api.CreatePartieRequestDto;
import fr.orleans.m1miage.trioweb.dtos.api.DistributionDto;
import fr.orleans.m1miage.trioweb.dtos.api.MoveResultDto;
import fr.orleans.m1miage.trioweb.dtos.api.PartieDto;
import fr.orleans.m1miage.trioweb.dtos.api.PlayMoveForm;
import fr.orleans.m1miage.trioweb.dtos.api.PlayMoveRequestDto;
import fr.orleans.m1miage.trioweb.modele.GameVariant;
import fr.orleans.m1miage.trioweb.service.PartieService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/games")
public class GamesController {

    private final PartieService partieService;

    public GamesController(PartieService partieService) {
        this.partieService = partieService;
    }

    @GetMapping
    public String lobby(Model model) {
        model.addAttribute("games", partieService.list());
        return "games/lobby";
    }

    @GetMapping("/finished")
    public String finished(Model model) {
        model.addAttribute("games", partieService.listFinished());
        return "games/finished";
    }

    @GetMapping("/state-version")
    @ResponseBody
    public String lobbyStateVersion() {
        return Integer.toHexString(signatureParties(partieService.list()).hashCode());
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        populateCreateForm(model, new CreateGameForm());
        return "games/create";
    }

    @PostMapping("/create")
    public String createSubmit(
            @Valid @ModelAttribute("form") CreateGameForm form,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            populateCreateForm(model, form);
            return "games/create";
        }
        if (form.isPrivateGame() && (form.getPassword() == null || form.getPassword().isBlank())) {
            result.rejectValue("password", "private.password.required", "Le mot de passe est obligatoire pour une partie privée.");
            populateCreateForm(model, form);
            return "games/create";
        }
        PartieDto createdPartie = partieService.create(
                new CreatePartieRequestDto(
                        form.getVariant().name(),
                        form.getMaxPlayers(),
                        form.isPrivateGame(),
                        form.getPassword()
                )
        );
        return "redirect:/games?created=" + createdPartie.getId();
    }

    @PostMapping("/{id}/join")
    public String join(
            @PathVariable Long id,
            @RequestParam(value = "motDePasse", required = false) String motDePasse,
            RedirectAttributes redirectAttributes
    ) {
        try {
            PartieDto joinedPartie = partieService.join(id, motDePasse);
            return "redirect:/games/" + joinedPartie.getId();
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("erreur", "Impossible de rejoindre : " + e.getStatusText());
            return "redirect:/games";
        }
    }

    @PostMapping("/{id}/start")
    public String start(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            partieService.start(id);
            return "redirect:/games/" + id;
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("erreur", "Impossible de démarrer : " + e.getStatusText());
            return "redirect:/games/" + id;
        }
    }

    @GetMapping("/{id}")
    public String game(@PathVariable Long id, Model model) {
        PartieDto partie = partieService.getById(id);
        model.addAttribute("partie", partie);
        if ("EN_COURS".equals(partie.getStatut()) || "TERMINEE".equals(partie.getStatut())) {
            try {
                DistributionDto distribution = partieService.getMyDistribution(id);
                model.addAttribute("distribution", distribution);
            } catch (HttpStatusCodeException e) {
                // Joueur non inscrit
            }
        }
        model.addAttribute("moveForm", new PlayMoveForm());
        return "games/game";
    }

    @GetMapping("/{id}/state-version")
    @ResponseBody
    public String stateVersion(@PathVariable Long id) {
        PartieDto partie = partieService.getById(id);
        StringBuilder version = new StringBuilder()
                .append(partie.getStatut()).append('|')
                .append(partie.getNombreJoueursActuels()).append('|')
                .append(partie.getNombreJoueursMax());

        if ("EN_COURS".equals(partie.getStatut()) || "TERMINEE".equals(partie.getStatut())) {
            try {
                DistributionDto distribution = partieService.getMyDistribution(id);
                version.append('|').append(distribution.getJoueurCourant())
                        .append('|').append(distribution.getDernierEvenement())
                        .append('|').append(distribution.getNombreCartesCentreCachees())
                        .append('|').append(signatureCartesRevelees(distribution))
                        .append('|').append(signatureTrios(distribution))
                        .append('|').append(signatureCartesParJoueur(distribution));
            } catch (HttpStatusCodeException e) {
                version.append("|no-distribution");
            }
        }

        return Integer.toHexString(version.toString().hashCode());
    }

    private String signatureCartesRevelees(DistributionDto distribution) {
        if (distribution.getCartesReveleesTour() == null || distribution.getCartesReveleesTour().isEmpty()) {
            return "revelees:none";
        }
        StringBuilder signature = new StringBuilder("revelees:");
        distribution.getCartesReveleesTour().forEach(carte -> signature
                .append(carte.getId()).append(',')
                .append(carte.getValeur()).append(',')
                .append(carte.getSource()).append(',')
                .append(carte.getProprietaire()).append(';'));
        return signature.toString();
    }

    private String signatureTrios(DistributionDto distribution) {
        if (distribution.getTriosParJoueur() == null || distribution.getTriosParJoueur().isEmpty()) {
            return "trios:none";
        }
        StringBuilder signature = new StringBuilder("trios:");
        distribution.getTriosParJoueur().forEach(trios -> {
            signature.append(trios.getJoueur()).append('=');
            List<Integer> valeurs = trios.getValeurs();
            if (valeurs != null) {
                valeurs.forEach(valeur -> signature.append(valeur).append(','));
            }
            signature.append(';');
        });
        return signature.toString();
    }

    private String signatureCartesParJoueur(DistributionDto distribution) {
        if (distribution.getNombreCartesParJoueur() == null || distribution.getNombreCartesParJoueur().isEmpty()) {
            return "mains:none";
        }
        StringBuilder signature = new StringBuilder("mains:");
        distribution.getNombreCartesParJoueur().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> signature.append(entry.getKey()).append('=').append(entry.getValue()).append(';'));
        return signature.toString();
    }

    private String signatureParties(List<PartieDto> parties) {
        if (parties == null || parties.isEmpty()) {
            return "parties:none";
        }
        StringBuilder signature = new StringBuilder("parties:");
        parties.stream()
                .sorted((a, b) -> a.getId().compareTo(b.getId()))
                .forEach(partie -> signature
                        .append(partie.getId()).append(',')
                        .append(partie.getStatut()).append(',')
                        .append(partie.getNombreJoueursActuels()).append(',')
                        .append(partie.getNombreJoueursMax()).append(',')
                        .append(partie.getCreateur()).append(',')
                        .append(partie.isPartiePrivee()).append(';'));
        return signature.toString();
    }

    @PostMapping("/{id}/moves")
    public String playMove(
            @PathVariable Long id,
            @ModelAttribute("moveForm") PlayMoveForm form,
            RedirectAttributes redirectAttributes
    ) {
        try {
            MoveResultDto result = partieService.playMove(
                    id,
                    new PlayMoveRequestDto(form.getType(), form.getCenterCardId(), form.getTargetPlayerPseudo())
            );
            if (result.isTrioRemporte()) {
                redirectAttributes.addFlashAttribute("trioRemporte", result.getValeurCarteRevelee());
            }
            if (result.isTourTermine() && !result.isTrioRemporte()) {
                redirectAttributes.addFlashAttribute("echec", "Numéros différents ! Tour passé.");
            }
            if (result.getGagnant() != null) {
                redirectAttributes.addFlashAttribute("gagnant", result.getGagnant());
            }
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("erreur", "Action invalide : " + e.getStatusText());
        }
        return "redirect:/games/" + id;
    }

    private void populateCreateForm(Model model, CreateGameForm form) {
        model.addAttribute("form", form);
        model.addAttribute("variants", GameVariant.values());
    }
}

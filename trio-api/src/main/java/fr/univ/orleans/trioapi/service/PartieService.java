package fr.univ.orleans.trioapi.service;

import fr.univ.orleans.trioapi.dto.partie.CarteCentreResponse;
import fr.univ.orleans.trioapi.dto.partie.CarteVisibleResponse;
import fr.univ.orleans.trioapi.dto.partie.CreatePartieRequest;
import fr.univ.orleans.trioapi.dto.partie.DistributionJoueurResponse;
import fr.univ.orleans.trioapi.dto.partie.JoinPartieRequest;
import fr.univ.orleans.trioapi.dto.partie.MoveResultResponse;
import fr.univ.orleans.trioapi.dto.partie.MoveType;
import fr.univ.orleans.trioapi.dto.partie.PartieResponse;
import fr.univ.orleans.trioapi.dto.partie.PlayMoveRequest;
import fr.univ.orleans.trioapi.dto.partie.TriosJoueurResponse;
import fr.univ.orleans.trioapi.dto.stats.StatistiquesJoueurResponse;
import fr.univ.orleans.trioapi.dto.stats.StatistiquesVarianteResponse;
import fr.univ.orleans.trioapi.model.AppUser;
import fr.univ.orleans.trioapi.model.CartePartie;
import fr.univ.orleans.trioapi.model.Partie;
import fr.univ.orleans.trioapi.model.PositionCarte;
import fr.univ.orleans.trioapi.model.StatutPartie;
import fr.univ.orleans.trioapi.model.VariantePartie;
import fr.univ.orleans.trioapi.repository.AppUserRepository;
import fr.univ.orleans.trioapi.repository.CartePartieRepository;
import fr.univ.orleans.trioapi.repository.PartieRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class PartieService {
    private static final int OBJECTIF_TRIOS_VARIANTE_A = 3;
    private static final int VALEUR_TRIO_INSTANTANE = 7;
    private static final Map<Integer, List<Integer>> LIAISONS_VARIANTE_B = Map.ofEntries(
        Map.entry(1, List.of(6, 8)),
        Map.entry(2, List.of(5, 9)),
        Map.entry(3, List.of(4, 10)),
        Map.entry(4, List.of(3, 11)),
        Map.entry(5, List.of(2, 12)),
        Map.entry(6, List.of(1)),
        Map.entry(7, List.of()),
        Map.entry(8, List.of(1)),
        Map.entry(9, List.of(2)),
        Map.entry(10, List.of(3)),
        Map.entry(11, List.of(4)),
        Map.entry(12, List.of(5))
    );
    private static final int DELAI_RESOLUTION_ECHEC_SECONDES = 3;

    private final PartieRepository partieRepository;
    private final AppUserRepository appUserRepository;
    private final CartePartieRepository cartePartieRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();
    private final Map<Integer, DistributionRegle> reglesDistribution = Map.of(
        3, new DistributionRegle(9, 9),
        4, new DistributionRegle(7, 8),
        5, new DistributionRegle(6, 6),
        6, new DistributionRegle(5, 6)
    );

    public PartieService(
        PartieRepository partieRepository,
        AppUserRepository appUserRepository,
        CartePartieRepository cartePartieRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.partieRepository = partieRepository;
        this.appUserRepository = appUserRepository;
        this.cartePartieRepository = cartePartieRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public PartieResponse creerPartie(CreatePartieRequest request, String userEmail) {
        AppUser createur = getUserByEmail(userEmail);
        Partie partie = new Partie(request.getVariante(), request.getNombreJoueurs(), createur);
        if (request.isPartiePrivee()) {
            String motDePasse = normaliserMotDePasse(request.getMotDePasse());
            if (motDePasse == null) {
                throw new ResponseStatusException(BAD_REQUEST, "Le mot de passe est obligatoire pour une partie privee");
            }
            partie.proteger(passwordEncoder.encode(motDePasse));
        }
        Partie saved = partieRepository.save(partie);
        return PartieResponse.from(saved);
    }

    public List<PartieResponse> listerParties() {
        return partieRepository.findAllByOrderByIdDesc().stream().map(PartieResponse::from).toList();
    }

    public List<PartieResponse> listerPartiesPourLobby(String userEmail) {
        AppUser joueur = getUserByEmail(userEmail);
        return partieRepository.findAllByOrderByIdDesc().stream()
            .filter(partie -> partie.getStatut() != StatutPartie.TERMINEE || partie.contientJoueur(joueur.getId()))
            .map(PartieResponse::from)
            .toList();
    }

    public List<PartieResponse> listerPartiesTerminees() {
        return partieRepository.findAllByOrderByIdDesc().stream()
            .filter(partie -> partie.getStatut() == StatutPartie.TERMINEE)
            .map(PartieResponse::from)
            .toList();
    }

    public StatistiquesJoueurResponse getStatistiquesJoueur(String userEmail) {
        AppUser joueur = getUserByEmail(userEmail);
        List<Partie> partiesJouees = partieRepository.findAllByOrderByIdDesc().stream()
            .filter(partie -> partie.getStatut() == StatutPartie.TERMINEE)
            .filter(partie -> partie.contientJoueur(joueur.getId()))
            .toList();

        long victoires = partiesJouees.stream()
            .filter(partie -> partie.getGagnant() != null && partie.getGagnant().getId().equals(joueur.getId()))
            .count();

        List<StatistiquesVarianteResponse> variantes = List.of(VariantePartie.A, VariantePartie.B).stream()
            .map(variante -> new StatistiquesVarianteResponse(
                variante.name(),
                partiesJouees.stream().filter(partie -> partie.getVariante() == variante).count(),
                partiesJouees.stream()
                    .filter(partie -> partie.getVariante() == variante)
                    .filter(partie -> partie.getGagnant() != null && partie.getGagnant().getId().equals(joueur.getId()))
                    .count()
            ))
            .toList();

        return new StatistiquesJoueurResponse(
            joueur.getEmail(),
            joueur.getPseudo(),
            partiesJouees.size(),
            victoires,
            variantes
        );
    }

    @Transactional
    public PartieResponse getPartie(Long partieId) {
        Partie partie = getPartieOrThrow(partieId);
        finaliserEchecSiExpire(partie);
        return PartieResponse.from(partie);
    }

    @Transactional
    public PartieResponse rejoindrePartie(Long partieId, String userEmail, JoinPartieRequest request) {
        Partie partie = getPartieOrThrow(partieId);

        if (partie.getStatut() != StatutPartie.EN_ATTENTE) {
            throw new ResponseStatusException(CONFLICT, "La partie n'est plus en attente");
        }

        AppUser joueur = getUserByEmail(userEmail);

        boolean dejaInscrit = partie.getJoueurs().stream().anyMatch(j -> j.getId().equals(joueur.getId()));
        if (dejaInscrit) {
            return PartieResponse.from(partie);
        }

        if (partie.isPartiePrivee()) {
            String motDePasse = request == null ? null : normaliserMotDePasse(request.getMotDePasse());
            if (motDePasse == null || !passwordEncoder.matches(motDePasse, partie.getMotDePasseHash())) {
                throw new ResponseStatusException(FORBIDDEN, "Mot de passe de partie incorrect");
            }
        }

        if (partie.estComplete()) {
            throw new ResponseStatusException(CONFLICT, "La partie est complete");
        }

        partie.ajouterJoueur(joueur);

        return PartieResponse.from(partie);
    }

    public PartieResponse rejoindrePartie(Long partieId, String userEmail) {
        return rejoindrePartie(partieId, userEmail, null);
    }

    @Transactional
    public PartieResponse lancerPartie(Long partieId, String userEmail) {
        Partie partie = getPartieOrThrow(partieId);
        AppUser joueur = getUserByEmail(userEmail);

        if (!partie.estCreateur(joueur.getId())) {
            throw new ResponseStatusException(FORBIDDEN, "Seul le createur peut lancer la partie");
        }
        if (partie.getStatut() != StatutPartie.EN_ATTENTE) {
            throw new ResponseStatusException(CONFLICT, "La partie est deja lancee");
        }
        if (!partie.estComplete()) {
            throw new ResponseStatusException(CONFLICT, "Nombre de joueurs insuffisant pour lancer la partie");
        }
        if (partie.isDistributionEffectuee()) {
            throw new ResponseStatusException(CONFLICT, "La distribution est deja effectuee");
        }

        distribuerCartes(partie);
        partie.demarrer(partie.getCreateur());
        return PartieResponse.from(partie);
    }

    @Transactional
    public DistributionJoueurResponse consulterDistribution(Long partieId, String userEmail) {
        Partie partie = getPartieOrThrow(partieId);
        finaliserEchecSiExpire(partie);
        AppUser joueur = getUserByEmail(userEmail);

        if (!partie.contientJoueur(joueur.getId())) {
            throw new ResponseStatusException(FORBIDDEN, "Vous n'etes pas inscrit a cette partie");
        }
        if (!partie.isDistributionEffectuee()) {
            throw new ResponseStatusException(CONFLICT, "La partie n'a pas encore commence");
        }

        List<Integer> maMain = cartePartieRepository
            .findByPartieIdAndProprietaireIdAndPositionOrderByValeurAsc(partieId, joueur.getId(), PositionCarte.MAIN_JOUEUR)
            .stream()
            .map(CartePartie::getValeur)
            .toList();

        List<Integer> valeursReveleesTour = getCartesReveleesTour(partieId).stream()
            .map(CartePartie::getValeur)
            .toList();

        List<CarteVisibleResponse> cartesReveleesTour = getCartesReveleesTour(partieId).stream()
            .map(this::toCarteVisibleResponse)
            .toList();

        List<CarteCentreResponse> cartesCentreCachees = cartePartieRepository
            .findByPartieIdAndPositionOrderByIdAsc(partieId, PositionCarte.CENTRE_CACHE)
            .stream()
            .map(carte -> new CarteCentreResponse(carte.getId()))
            .toList();

        List<CarteCentreResponse> cartesCentre = cartePartieRepository
            .findByPartieIdAndPositionInOrderByIdAsc(
                partieId,
                List.of(PositionCarte.CENTRE_CACHE, PositionCarte.CENTRE_REVELEE)
            )
            .stream()
            .map(this::toCarteCentreResponse)
            .toList();

        List<TriosJoueurResponse> triosParJoueur = getTriosParJoueur(partieId);

        long nombreCartesCentreCachees = cartePartieRepository.countByPartieIdAndPosition(partieId, PositionCarte.CENTRE_CACHE);

        Map<String, Long> cartesParJoueur = new HashMap<>();
        for (CartePartie carte : cartePartieRepository.findByPartieIdAndPosition(partieId, PositionCarte.MAIN_JOUEUR)) {
            String pseudo = carte.getProprietaire().getPseudo();
            cartesParJoueur.merge(pseudo, 1L, Long::sum);
        }

        return new DistributionJoueurResponse(
            partie.getId(),
            partie.getStatut().name(),
            partie.getVariante().name(),
            joueur.getPseudo(),
            partie.getJoueurCourant() == null ? null : partie.getJoueurCourant().getPseudo(),
            maMain,
            valeursReveleesTour,
            cartesReveleesTour,
            cartesCentre,
            cartesCentreCachees,
            triosParJoueur,
            nombreCartesCentreCachees,
            cartesParJoueur,
            partie.getDernierEvenement()
        );
    }

    private CarteCentreResponse toCarteCentreResponse(CartePartie carte) {
        boolean revelee = carte.getPosition() == PositionCarte.CENTRE_REVELEE;
        return new CarteCentreResponse(carte.getId(), revelee ? carte.getValeur() : null, revelee);
    }

    @Transactional
    public MoveResultResponse jouerCoup(Long partieId, String userEmail, PlayMoveRequest request) {
        Partie partie = getPartieOrThrow(partieId);
        finaliserEchecSiExpire(partie);
        AppUser joueur = getUserByEmail(userEmail);

        if (partie.getStatut() != StatutPartie.EN_COURS) {
            throw new ResponseStatusException(CONFLICT, "La partie n'est pas en cours");
        }
        if (partie.hasEchecEnAttente()) {
            throw new ResponseStatusException(CONFLICT, "Le tour est en cours de resolution");
        }
        if (!partie.contientJoueur(joueur.getId())) {
            throw new ResponseStatusException(FORBIDDEN, "Vous n'etes pas inscrit a cette partie");
        }
        if (partie.getJoueurCourant() == null || !partie.getJoueurCourant().getId().equals(joueur.getId())) {
            throw new ResponseStatusException(FORBIDDEN, "Ce n'est pas votre tour");
        }

        CartePartie carteRevelee;
        String joueurCible = null;
        String actionEvenement;

        switch (request.getType()) {
            case REVEAL_CENTER -> {
                carteRevelee = revelerDepuisCentre(partieId, request.getCenterCardId());
                actionEvenement = joueur.getPseudo() + " révèle une carte du centre : " + carteRevelee.getValeur() + ".";
            }
            case REVEAL_PLAYER_MIN -> {
                AppUser cible = getJoueurCibleDansPartie(partie, request.getTargetPlayerPseudo());
                carteRevelee = revelerDepuisMain(partieId, cible.getId(), true);
                joueurCible = cible.getPseudo();
                actionEvenement = joueur.getPseudo() + " révèle le MIN de " + joueurCible + " : " + carteRevelee.getValeur() + ".";
            }
            case REVEAL_PLAYER_MAX -> {
                AppUser cible = getJoueurCibleDansPartie(partie, request.getTargetPlayerPseudo());
                carteRevelee = revelerDepuisMain(partieId, cible.getId(), false);
                joueurCible = cible.getPseudo();
                actionEvenement = joueur.getPseudo() + " révèle le MAX de " + joueurCible + " : " + carteRevelee.getValeur() + ".";
            }
            default -> throw new ResponseStatusException(CONFLICT, "Type de coup non supporte");
        }

        ResolutionTour resolution = resoudreTourSiNecessaire(partie, joueur);
        partie.noterEvenement(actionEvenement + getSuffixeResolution(resolution));
        long cartesCentreCacheesRestantes = cartePartieRepository.countByPartieIdAndPosition(partieId, PositionCarte.CENTRE_CACHE);

        List<Integer> valeursRevelees = resolution.valeursRevelees();

        return new MoveResultResponse(
            partie.getId(),
            partie.getStatut().name(),
            request.getType().name(),
            joueur.getPseudo(),
            joueurCible,
            carteRevelee.getId(),
            carteRevelee.getValeur(),
            valeursRevelees,
            resolution.tourTermine(),
            resolution.type().name(),
            resolution.type() == TypeResolution.TRIO_GAGNE,
            partie.getJoueurCourant() == null ? null : partie.getJoueurCourant().getPseudo(),
            partie.getNumeroTour(),
            cartesCentreCacheesRestantes,
            partie.getGagnant() == null ? null : partie.getGagnant().getPseudo(),
            partie.getConditionVictoire()
        );
    }

    private void distribuerCartes(Partie partie) {
        List<AppUser> ordreJoueurs = getOrdreJoueurs(partie);
        DistributionRegle regle = reglesDistribution.get(ordreJoueurs.size());
        if (regle == null) {
            throw new ResponseStatusException(CONFLICT, "Nombre de joueurs invalide pour la distribution");
        }

        List<Integer> paquet = creerPaquetMelange();
        List<CartePartie> cartesDistribuees = new ArrayList<>(36);
        int index = 0;

        for (AppUser joueur : ordreJoueurs) {
            for (int i = 0; i < regle.cartesParJoueur(); i++) {
                cartesDistribuees.add(CartePartie.pourMainJoueur(partie, joueur, paquet.get(index++)));
            }
        }
        for (int i = 0; i < regle.cartesCentreCachees(); i++) {
            cartesDistribuees.add(CartePartie.pourCentreCache(partie, paquet.get(index++)));
        }

        if (index != paquet.size()) {
            throw new IllegalStateException("Distribution invalide: nombre total de cartes incorrect");
        }

        cartePartieRepository.saveAll(cartesDistribuees);
    }

    private List<AppUser> getOrdreJoueurs(Partie partie) {
        List<AppUser> joueurs = partie.getJoueurs().stream()
            .sorted(Comparator.comparing(AppUser::getId))
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        joueurs.removeIf(joueur -> joueur.getId().equals(partie.getCreateur().getId()));
        joueurs.addFirst(partie.getCreateur());
        return joueurs;
    }

    private AppUser getJoueurSuivant(Partie partie, Long joueurCourantId) {
        List<AppUser> ordreJoueurs = getOrdreJoueurs(partie);
        for (int i = 0; i < ordreJoueurs.size(); i++) {
            AppUser joueur = ordreJoueurs.get(i);
            if (joueur.getId().equals(joueurCourantId)) {
                return ordreJoueurs.get((i + 1) % ordreJoueurs.size());
            }
        }
        throw new IllegalStateException("Joueur courant introuvable dans la partie");
    }

    private CartePartie revelerDepuisCentre(Long partieId, Long centerCardId) {
        CartePartie carte = centerCardId == null
            ? cartePartieRepository.findFirstByPartieIdAndPositionOrderByIdAsc(partieId, PositionCarte.CENTRE_CACHE)
                .orElseThrow(() -> new ResponseStatusException(CONFLICT, "Aucune carte disponible au centre"))
            : cartePartieRepository.findByIdAndPartieIdAndPosition(centerCardId, partieId, PositionCarte.CENTRE_CACHE)
                .orElseThrow(() -> new ResponseStatusException(CONFLICT, "Carte centre invalide ou deja revelee"));

        carte.revelerDepuisCentre();
        return carte;
    }

    private CartePartie revelerDepuisMain(Long partieId, Long cibleId, boolean minimum) {
        return (minimum
            ? cartePartieRepository.findFirstByPartieIdAndProprietaireIdAndPositionOrderByValeurAsc(
                partieId, cibleId, PositionCarte.MAIN_JOUEUR
            )
            : cartePartieRepository.findFirstByPartieIdAndProprietaireIdAndPositionOrderByValeurDesc(
                partieId, cibleId, PositionCarte.MAIN_JOUEUR
            ))
            .map(carte -> {
                carte.revelerDepuisMain();
                return carte;
            })
            .orElseThrow(() -> new ResponseStatusException(CONFLICT, "Le joueur cible n'a pas de carte en main"));
    }

    private ResolutionTour resoudreTourSiNecessaire(Partie partie, AppUser joueur) {
        List<CartePartie> reveles = getCartesReveleesTour(partie.getId());
        List<Integer> valeurs = reveles.stream().map(CartePartie::getValeur).toList();
        long distincts = valeurs.stream().distinct().count();

        if (distincts >= 2) {
            AppUser prochainJoueur = getJoueurSuivant(partie, joueur.getId());
            for (CartePartie carte : reveles) {
                carte.reCacher();
            }
            partie.terminerResolutionEchec();
            partie.passerAuJoueurSuivant(prochainJoueur);
            terminerPartieSiAucuneCarteJouable(partie);
            return new ResolutionTour(TypeResolution.ECHEC_NUMEROS_DIFFERENTS, true, valeurs);
        }

        if (valeurs.size() == 3) {
            for (CartePartie carte : reveles) {
                carte.attribuerAuTrio(joueur);
            }
            cartePartieRepository.saveAll(reveles);
            ConditionVictoire conditionVictoire = getConditionVictoireApresTrio(partie, joueur);
            if (conditionVictoire != null) {
                partie.terminer(joueur, conditionVictoire.name());
            } else {
                partie.passerAuJoueurSuivant(getJoueurSuivant(partie, joueur.getId()));
                terminerPartieSiAucuneCarteJouable(partie);
            }
            return new ResolutionTour(TypeResolution.TRIO_GAGNE, true, valeurs);
        }

        return new ResolutionTour(TypeResolution.EN_ATTENTE, false, valeurs);
    }

    private ConditionVictoire getConditionVictoireApresTrio(Partie partie, AppUser joueur) {
        List<Integer> valeursTrios = cartePartieRepository
            .findByPartieIdAndProprietaireIdAndPositionOrderByValeurAsc(partie.getId(), joueur.getId(), PositionCarte.TRIO_GAGNE)
            .stream()
            .map(CartePartie::getValeur)
            .distinct()
            .toList();

        if (valeursTrios.contains(VALEUR_TRIO_INSTANTANE)) {
            return ConditionVictoire.TRIO_DE_7;
        }

        if (partie.getVariante() == VariantePartie.A) {
            return valeursTrios.size() >= OBJECTIF_TRIOS_VARIANTE_A ? ConditionVictoire.TROIS_TRIOS : null;
        }

        return hasTrioLie(valeursTrios) ? ConditionVictoire.DEUX_TRIOS_LIES : null;
    }

    private void terminerPartieSiAucuneCarteJouable(Partie partie) {
        long cartesMains = cartePartieRepository.countByPartieIdAndPosition(partie.getId(), PositionCarte.MAIN_JOUEUR);
        long cartesCentre = cartePartieRepository.countByPartieIdAndPosition(partie.getId(), PositionCarte.CENTRE_CACHE);
        if (cartesMains == 0 && cartesCentre == 0) {
            partie.terminer(null, ConditionVictoire.AUCUNE_CARTE_JOUABLE.name());
        }
    }

    private void finaliserEchecSiExpire(Partie partie) {
        if (!partie.hasEchecEnAttente() || Instant.now().isBefore(partie.getResolutionEchecApres())) {
            return;
        }

        for (CartePartie carte : getCartesReveleesTour(partie.getId())) {
            carte.reCacher();
        }

        AppUser prochainJoueur = partie.getJoueurApresResolution();
        partie.terminerResolutionEchec();
        partie.passerAuJoueurSuivant(prochainJoueur);
        terminerPartieSiAucuneCarteJouable(partie);
        if (partie.getStatut() == StatutPartie.EN_COURS && partie.getJoueurCourant() != null) {
            partie.noterEvenement("Tour de " + partie.getJoueurCourant().getPseudo() + ".");
        }
    }

    private boolean hasTrioLie(List<Integer> valeursTrios) {
        for (int valeur : valeursTrios) {
            for (int valeurLiee : LIAISONS_VARIANTE_B.getOrDefault(valeur, List.of())) {
                if (valeursTrios.contains(valeurLiee)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<CartePartie> getCartesReveleesTour(Long partieId) {
        List<CartePartie> depuisMain = cartePartieRepository.findByPartieIdAndPositionOrderByIdAsc(
            partieId,
            PositionCarte.MAIN_REVELEE
        );
        List<CartePartie> depuisCentre = cartePartieRepository.findByPartieIdAndPositionOrderByIdAsc(
            partieId,
            PositionCarte.CENTRE_REVELEE
        );

        List<CartePartie> toutes = new ArrayList<>(depuisMain.size() + depuisCentre.size());
        toutes.addAll(depuisMain);
        toutes.addAll(depuisCentre);
        toutes.sort(Comparator.comparing(CartePartie::getId));
        return toutes;
    }

    private CarteVisibleResponse toCarteVisibleResponse(CartePartie carte) {
        if (carte.getPosition() == PositionCarte.CENTRE_REVELEE) {
            return new CarteVisibleResponse(
                carte.getId(),
                carte.getValeur(),
                "CENTRE",
                null,
                "Centre"
            );
        }

        String proprietaire = carte.getProprietaire() == null ? null : carte.getProprietaire().getPseudo();
        return new CarteVisibleResponse(
            carte.getId(),
            carte.getValeur(),
            "MAIN",
            proprietaire,
            proprietaire == null ? "Main" : "Main de " + proprietaire
        );
    }

    private List<TriosJoueurResponse> getTriosParJoueur(Long partieId) {
        Map<String, List<Integer>> valeursParJoueur = new HashMap<>();
        for (CartePartie carte : cartePartieRepository.findByPartieIdAndPosition(partieId, PositionCarte.TRIO_GAGNE)) {
            if (carte.getProprietaire() == null) {
                continue;
            }
            String pseudo = carte.getProprietaire().getPseudo();
            List<Integer> valeurs = valeursParJoueur.computeIfAbsent(pseudo, key -> new ArrayList<>());
            if (!valeurs.contains(carte.getValeur())) {
                valeurs.add(carte.getValeur());
            }
        }

        return valeursParJoueur.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                List<Integer> valeurs = entry.getValue().stream().sorted().toList();
                return new TriosJoueurResponse(entry.getKey(), valeurs);
            })
            .toList();
    }

    private String getSuffixeResolution(ResolutionTour resolution) {
        return switch (resolution.type()) {
            case EN_ATTENTE -> "";
            case ECHEC_NUMEROS_DIFFERENTS -> " Numéros différents, tour passé.";
            case TRIO_GAGNE -> " Trio gagné.";
        };
    }

    private AppUser getJoueurCibleDansPartie(Partie partie, String pseudo) {
        if (pseudo == null || pseudo.isBlank()) {
            throw new ResponseStatusException(CONFLICT, "Le pseudo cible est obligatoire");
        }
        return partie.getJoueurs().stream()
            .filter(j -> j.getPseudo().equalsIgnoreCase(pseudo.trim()))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(CONFLICT, "Joueur cible introuvable dans cette partie"));
    }

    private List<Integer> creerPaquetMelange() {
        List<Integer> paquet = new ArrayList<>(36);
        for (int valeur = 1; valeur <= 12; valeur++) {
            paquet.add(valeur);
            paquet.add(valeur);
            paquet.add(valeur);
        }
        for (int i = paquet.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = paquet.get(i);
            paquet.set(i, paquet.get(j));
            paquet.set(j, tmp);
        }
        return paquet;
    }

    private Partie getPartieOrThrow(Long partieId) {
        return partieRepository.findById(partieId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Partie introuvable"));
    }

    private AppUser getUserByEmail(String email) {
        return appUserRepository.findByEmail(normalizeEmail(email))
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Utilisateur introuvable"));
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String normaliserMotDePasse(String motDePasse) {
        if (motDePasse == null || motDePasse.isBlank()) {
            return null;
        }
        return motDePasse.trim();
    }

    private record DistributionRegle(int cartesParJoueur, int cartesCentreCachees) {
    }

    private enum TypeResolution {
        EN_ATTENTE,
        ECHEC_NUMEROS_DIFFERENTS,
        TRIO_GAGNE
    }

    private enum ConditionVictoire {
        TROIS_TRIOS,
        DEUX_TRIOS_LIES,
        TRIO_DE_7,
        AUCUNE_CARTE_JOUABLE
    }

    private record ResolutionTour(TypeResolution type, boolean tourTermine, List<Integer> valeursRevelees) {
    }
}

package fr.univ.orleans.trioapi.dto.partie;

import java.util.List;
import java.util.Map;

public class DistributionJoueurResponse {

    private final Long partieId;
    private final String statut;
    private final String variante;
    private final String monPseudo;
    private final String joueurCourant;
    private final List<Integer> maMain;
    private final List<Integer> valeursReveleesTour;
    private final List<CarteVisibleResponse> cartesReveleesTour;
    private final List<CarteCentreResponse> cartesCentre;
    private final List<CarteCentreResponse> cartesCentreCachees;
    private final List<TriosJoueurResponse> triosParJoueur;
    private final long nombreCartesCentreCachees;
    private final Map<String, Long> nombreCartesParJoueur;
    private final String dernierEvenement;

    public DistributionJoueurResponse(
        Long partieId,
        String statut,
        String variante,
        String monPseudo,
        String joueurCourant,
        List<Integer> maMain,
        List<Integer> valeursReveleesTour,
        List<CarteVisibleResponse> cartesReveleesTour,
        List<CarteCentreResponse> cartesCentre,
        List<CarteCentreResponse> cartesCentreCachees,
        List<TriosJoueurResponse> triosParJoueur,
        long nombreCartesCentreCachees,
        Map<String, Long> nombreCartesParJoueur,
        String dernierEvenement
    ) {
        this.partieId = partieId;
        this.statut = statut;
        this.variante = variante;
        this.monPseudo = monPseudo;
        this.joueurCourant = joueurCourant;
        this.maMain = maMain;
        this.valeursReveleesTour = valeursReveleesTour;
        this.cartesReveleesTour = cartesReveleesTour;
        this.cartesCentre = cartesCentre;
        this.cartesCentreCachees = cartesCentreCachees;
        this.triosParJoueur = triosParJoueur;
        this.nombreCartesCentreCachees = nombreCartesCentreCachees;
        this.nombreCartesParJoueur = nombreCartesParJoueur;
        this.dernierEvenement = dernierEvenement;
    }

    public Long getPartieId() {
        return partieId;
    }

    public String getStatut() {
        return statut;
    }

    public String getVariante() {
        return variante;
    }

    public String getMonPseudo() {
        return monPseudo;
    }

    public String getJoueurCourant() {
        return joueurCourant;
    }

    public List<Integer> getMaMain() {
        return maMain;
    }

    public List<Integer> getValeursReveleesTour() {
        return valeursReveleesTour;
    }

    public List<CarteVisibleResponse> getCartesReveleesTour() {
        return cartesReveleesTour;
    }

    public List<CarteCentreResponse> getCartesCentre() {
        return cartesCentre;
    }

    public List<CarteCentreResponse> getCartesCentreCachees() {
        return cartesCentreCachees;
    }

    public List<TriosJoueurResponse> getTriosParJoueur() {
        return triosParJoueur;
    }

    public long getNombreCartesCentreCachees() {
        return nombreCartesCentreCachees;
    }

    public Map<String, Long> getNombreCartesParJoueur() {
        return nombreCartesParJoueur;
    }

    public String getDernierEvenement() {
        return dernierEvenement;
    }
}

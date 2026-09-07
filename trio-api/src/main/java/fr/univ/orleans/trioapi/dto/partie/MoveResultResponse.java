package fr.univ.orleans.trioapi.dto.partie;

import java.util.List;

public class MoveResultResponse {

    private final Long partieId;
    private final String statut;
    private final String type;
    private final String joueur;
    private final String joueurCible;
    private final Long carteReveleeId;
    private final int valeurCarteRevelee;
    private final List<Integer> valeursReveleesTour;
    private final boolean tourTermine;
    private final String resolutionTour;
    private final boolean trioRemporte;
    private final String joueurCourant;
    private final int numeroTour;
    private final long cartesCentreCacheesRestantes;
    private final String gagnant;
    private final String conditionVictoire;

    public MoveResultResponse(
        Long partieId,
        String statut,
        String type,
        String joueur,
        String joueurCible,
        Long carteReveleeId,
        int valeurCarteRevelee,
        List<Integer> valeursReveleesTour,
        boolean tourTermine,
        String resolutionTour,
        boolean trioRemporte,
        String joueurCourant,
        int numeroTour,
        long cartesCentreCacheesRestantes,
        String gagnant,
        String conditionVictoire
    ) {
        this.partieId = partieId;
        this.statut = statut;
        this.type = type;
        this.joueur = joueur;
        this.joueurCible = joueurCible;
        this.carteReveleeId = carteReveleeId;
        this.valeurCarteRevelee = valeurCarteRevelee;
        this.valeursReveleesTour = valeursReveleesTour;
        this.tourTermine = tourTermine;
        this.resolutionTour = resolutionTour;
        this.trioRemporte = trioRemporte;
        this.joueurCourant = joueurCourant;
        this.numeroTour = numeroTour;
        this.cartesCentreCacheesRestantes = cartesCentreCacheesRestantes;
        this.gagnant = gagnant;
        this.conditionVictoire = conditionVictoire;
    }

    public Long getPartieId() {
        return partieId;
    }

    public String getStatut() {
        return statut;
    }

    public String getType() {
        return type;
    }

    public String getJoueur() {
        return joueur;
    }

    public String getJoueurCible() {
        return joueurCible;
    }

    public Long getCarteReveleeId() {
        return carteReveleeId;
    }

    public int getValeurCarteRevelee() {
        return valeurCarteRevelee;
    }

    public List<Integer> getValeursReveleesTour() {
        return valeursReveleesTour;
    }

    public boolean isTourTermine() {
        return tourTermine;
    }

    public String getResolutionTour() {
        return resolutionTour;
    }

    public boolean isTrioRemporte() {
        return trioRemporte;
    }

    public String getJoueurCourant() {
        return joueurCourant;
    }

    public int getNumeroTour() {
        return numeroTour;
    }

    public long getCartesCentreCacheesRestantes() {
        return cartesCentreCacheesRestantes;
    }

    public String getGagnant() {
        return gagnant;
    }

    public String getConditionVictoire() {
        return conditionVictoire;
    }
}

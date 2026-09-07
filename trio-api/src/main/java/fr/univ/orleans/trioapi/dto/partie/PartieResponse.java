package fr.univ.orleans.trioapi.dto.partie;

import fr.univ.orleans.trioapi.model.Partie;

public class PartieResponse {

    private final Long id;
    private final String variante;
    private final String statut;
    private final int nombreJoueursMax;
    private final int nombreJoueursActuels;
    private final String createur;
    private final boolean distributionEffectuee;
    private final boolean partiePrivee;
    private final String joueurCourant;
    private final String gagnant;
    private final String conditionVictoire;

    public PartieResponse(
        Long id,
        String variante,
        String statut,
        int nombreJoueursMax,
        int nombreJoueursActuels,
        String createur,
        boolean distributionEffectuee,
        boolean partiePrivee,
        String joueurCourant,
        String gagnant,
        String conditionVictoire
    ) {
        this.id = id;
        this.variante = variante;
        this.statut = statut;
        this.nombreJoueursMax = nombreJoueursMax;
        this.nombreJoueursActuels = nombreJoueursActuels;
        this.createur = createur;
        this.distributionEffectuee = distributionEffectuee;
        this.partiePrivee = partiePrivee;
        this.joueurCourant = joueurCourant;
        this.gagnant = gagnant;
        this.conditionVictoire = conditionVictoire;
    }

    public static PartieResponse from(Partie partie) {
        return new PartieResponse(
            partie.getId(),
            partie.getVariante().name(),
            partie.getStatut().name(),
            partie.getNombreJoueursMax(),
            partie.getNombreJoueursActuels(),
            partie.getCreateur().getPseudo(),
            partie.isDistributionEffectuee(),
            partie.isPartiePrivee(),
            partie.getJoueurCourant() == null ? null : partie.getJoueurCourant().getPseudo(),
            partie.getGagnant() == null ? null : partie.getGagnant().getPseudo(),
            partie.getConditionVictoire()
        );
    }

    public Long getId() {
        return id;
    }

    public String getVariante() {
        return variante;
    }

    public String getStatut() {
        return statut;
    }

    public int getNombreJoueursMax() {
        return nombreJoueursMax;
    }

    public int getNombreJoueursActuels() {
        return nombreJoueursActuels;
    }

    public String getCreateur() {
        return createur;
    }

    public boolean isDistributionEffectuee() {
        return distributionEffectuee;
    }

    public boolean isPartiePrivee() {
        return partiePrivee;
    }

    public String getJoueurCourant() {
        return joueurCourant;
    }

    public String getGagnant() {
        return gagnant;
    }

    public String getConditionVictoire() {
        return conditionVictoire;
    }
}

package fr.orleans.m1miage.trioweb.dtos.api;

public class PartieDto {
    private Long id;
    private String variante;
    private String statut;
    private Integer nombreJoueursMax;
    private Integer nombreJoueursActuels;
    private String createur;
    private boolean distributionEffectuee;
    private boolean partiePrivee;
    private String joueurCourant;
    private String gagnant;
    private String conditionVictoire;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVariante() { return variante; }
    public void setVariante(String variante) { this.variante = variante; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Integer getNombreJoueursMax() { return nombreJoueursMax; }
    public void setNombreJoueursMax(Integer nombreJoueursMax) { this.nombreJoueursMax = nombreJoueursMax; }

    public Integer getNombreJoueursActuels() { return nombreJoueursActuels; }
    public void setNombreJoueursActuels(Integer nombreJoueursActuels) { this.nombreJoueursActuels = nombreJoueursActuels; }

    public String getCreateur() { return createur; }
    public void setCreateur(String createur) { this.createur = createur; }

    public boolean isDistributionEffectuee() { return distributionEffectuee; }
    public void setDistributionEffectuee(boolean distributionEffectuee) { this.distributionEffectuee = distributionEffectuee; }

    public boolean isPartiePrivee() { return partiePrivee; }
    public void setPartiePrivee(boolean partiePrivee) { this.partiePrivee = partiePrivee; }

    public String getJoueurCourant() { return joueurCourant; }
    public void setJoueurCourant(String joueurCourant) { this.joueurCourant = joueurCourant; }

    public String getGagnant() { return gagnant; }
    public void setGagnant(String gagnant) { this.gagnant = gagnant; }

    public String getConditionVictoire() { return conditionVictoire; }
    public void setConditionVictoire(String conditionVictoire) { this.conditionVictoire = conditionVictoire; }
}

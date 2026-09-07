package fr.orleans.m1miage.trioweb.dtos.api;

import java.util.List;

public class MoveResultDto {

    private Long partieId;
    private String statut;
    private String type;
    private String joueur;
    private String joueurCible;
    private Long carteReveleeId;
    private int valeurCarteRevelee;
    private List<Integer> valeursReveleesTour;
    private boolean tourTermine;
    private String resolutionTour;
    private boolean trioRemporte;
    private String joueurCourant;
    private int numeroTour;
    private long cartesCentreCacheesRestantes;
    private String gagnant;
    private String conditionVictoire;

    public Long getPartieId() { return partieId; }
    public void setPartieId(Long partieId) { this.partieId = partieId; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getJoueur() { return joueur; }
    public void setJoueur(String joueur) { this.joueur = joueur; }

    public String getJoueurCible() { return joueurCible; }
    public void setJoueurCible(String joueurCible) { this.joueurCible = joueurCible; }

    public Long getCarteReveleeId() { return carteReveleeId; }
    public void setCarteReveleeId(Long carteReveleeId) { this.carteReveleeId = carteReveleeId; }

    public int getValeurCarteRevelee() { return valeurCarteRevelee; }
    public void setValeurCarteRevelee(int valeurCarteRevelee) { this.valeurCarteRevelee = valeurCarteRevelee; }

    public List<Integer> getValeursReveleesTour() { return valeursReveleesTour; }
    public void setValeursReveleesTour(List<Integer> valeursReveleesTour) { this.valeursReveleesTour = valeursReveleesTour; }

    public boolean isTourTermine() { return tourTermine; }
    public void setTourTermine(boolean tourTermine) { this.tourTermine = tourTermine; }

    public String getResolutionTour() { return resolutionTour; }
    public void setResolutionTour(String resolutionTour) { this.resolutionTour = resolutionTour; }

    public boolean isTrioRemporte() { return trioRemporte; }
    public void setTrioRemporte(boolean trioRemporte) { this.trioRemporte = trioRemporte; }

    public String getJoueurCourant() { return joueurCourant; }
    public void setJoueurCourant(String joueurCourant) { this.joueurCourant = joueurCourant; }

    public int getNumeroTour() { return numeroTour; }
    public void setNumeroTour(int numeroTour) { this.numeroTour = numeroTour; }

    public long getCartesCentreCacheesRestantes() { return cartesCentreCacheesRestantes; }
    public void setCartesCentreCacheesRestantes(long n) { this.cartesCentreCacheesRestantes = n; }

    public String getGagnant() { return gagnant; }
    public void setGagnant(String gagnant) { this.gagnant = gagnant; }

    public String getConditionVictoire() { return conditionVictoire; }
    public void setConditionVictoire(String conditionVictoire) { this.conditionVictoire = conditionVictoire; }
}
package fr.orleans.m1miage.trioweb.dtos.api;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DistributionDto {

    private Long partieId;
    private String statut;
    private String variante;
    private String monPseudo;
    private String joueurCourant;
    private List<Integer> maMain;
    private List<Integer> valeursReveleesTour;
    private List<CarteVisibleDto> cartesReveleesTour;
    private List<CarteCentreDto> cartesCentre;
    private List<CarteCentreDto> cartesCentreCachees;
    private List<TriosJoueurDto> triosParJoueur;
    private long nombreCartesCentreCachees;
    private Map<String, Long> nombreCartesParJoueur;
    private String dernierEvenement;

    public Long getPartieId() { return partieId; }
    public void setPartieId(Long partieId) { this.partieId = partieId; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getVariante() { return variante; }
    public void setVariante(String variante) { this.variante = variante; }

    public String getMonPseudo() { return monPseudo; }
    public void setMonPseudo(String monPseudo) { this.monPseudo = monPseudo; }

    public String getJoueurCourant() { return joueurCourant; }
    public void setJoueurCourant(String joueurCourant) { this.joueurCourant = joueurCourant; }

    public List<Integer> getMaMain() { return maMain; }
    public void setMaMain(List<Integer> maMain) { this.maMain = maMain; }

    public List<Integer> getValeursReveleesTour() { return valeursReveleesTour; }
    public void setValeursReveleesTour(List<Integer> valeursReveleesTour) { this.valeursReveleesTour = valeursReveleesTour; }

    public List<CarteVisibleDto> getCartesReveleesTour() { return cartesReveleesTour; }
    public void setCartesReveleesTour(List<CarteVisibleDto> cartesReveleesTour) { this.cartesReveleesTour = cartesReveleesTour; }

    public List<CarteCentreDto> getCartesCentre() { return cartesCentre; }
    public void setCartesCentre(List<CarteCentreDto> cartesCentre) { this.cartesCentre = cartesCentre; }

    public List<CarteCentreDto> getCartesCentreCachees() { return cartesCentreCachees; }
    public void setCartesCentreCachees(List<CarteCentreDto> cartesCentreCachees) { this.cartesCentreCachees = cartesCentreCachees; }

    public List<TriosJoueurDto> getTriosParJoueur() { return triosParJoueur; }
    public void setTriosParJoueur(List<TriosJoueurDto> triosParJoueur) { this.triosParJoueur = triosParJoueur; }

    public long getNombreCartesCentreCachees() { return nombreCartesCentreCachees; }
    public void setNombreCartesCentreCachees(long n) { this.nombreCartesCentreCachees = n; }

    public Map<String, Long> getNombreCartesParJoueur() { return nombreCartesParJoueur; }
    public void setNombreCartesParJoueur(Map<String, Long> m) { this.nombreCartesParJoueur = m; }

    public String getDernierEvenement() { return dernierEvenement; }
    public void setDernierEvenement(String dernierEvenement) { this.dernierEvenement = dernierEvenement; }

    public List<Entry<String, Long>> getAdversaires() {
        if (nombreCartesParJoueur == null) {
            return List.of();
        }
        return nombreCartesParJoueur.entrySet().stream()
                .filter(entry -> monPseudo == null || !monPseudo.equals(entry.getKey()))
                .sorted(Entry.comparingByKey())
                .toList();
    }
}

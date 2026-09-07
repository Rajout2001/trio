package fr.univ.orleans.trioapi.dto.partie;

public class CarteVisibleResponse {

    private final Long id;
    private final int valeur;
    private final String source;
    private final String proprietaire;
    private final String libelle;

    public CarteVisibleResponse(Long id, int valeur, String source, String proprietaire, String libelle) {
        this.id = id;
        this.valeur = valeur;
        this.source = source;
        this.proprietaire = proprietaire;
        this.libelle = libelle;
    }

    public Long getId() {
        return id;
    }

    public int getValeur() {
        return valeur;
    }

    public String getSource() {
        return source;
    }

    public String getProprietaire() {
        return proprietaire;
    }

    public String getLibelle() {
        return libelle;
    }
}

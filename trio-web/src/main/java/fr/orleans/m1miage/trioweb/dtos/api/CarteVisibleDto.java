package fr.orleans.m1miage.trioweb.dtos.api;

public class CarteVisibleDto {

    private Long id;
    private int valeur;
    private String source;
    private String proprietaire;
    private String libelle;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getValeur() { return valeur; }
    public void setValeur(int valeur) { this.valeur = valeur; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getProprietaire() { return proprietaire; }
    public void setProprietaire(String proprietaire) { this.proprietaire = proprietaire; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
}

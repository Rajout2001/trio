package fr.orleans.m1miage.trioweb.dtos.api;

public class CarteCentreDto {

    private Long id;
    private Integer valeur;
    private boolean revelee;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getValeur() { return valeur; }
    public void setValeur(Integer valeur) { this.valeur = valeur; }

    public boolean isRevelee() { return revelee; }
    public void setRevelee(boolean revelee) { this.revelee = revelee; }
}

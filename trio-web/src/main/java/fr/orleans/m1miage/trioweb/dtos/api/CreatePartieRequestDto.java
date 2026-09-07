package fr.orleans.m1miage.trioweb.dtos.api;

public class CreatePartieRequestDto {

    private String variante;
    private Integer nombreJoueurs;
    private boolean partiePrivee;
    private String motDePasse;

    public CreatePartieRequestDto() {
    }

    public CreatePartieRequestDto(String variante, Integer nombreJoueurs) {
        this.variante = variante;
        this.nombreJoueurs = nombreJoueurs;
    }

    public CreatePartieRequestDto(String variante, Integer nombreJoueurs, boolean partiePrivee, String motDePasse) {
        this.variante = variante;
        this.nombreJoueurs = nombreJoueurs;
        this.partiePrivee = partiePrivee;
        this.motDePasse = motDePasse;
    }

    public String getVariante() {
        return variante;
    }

    public void setVariante(String variante) {
        this.variante = variante;
    }

    public Integer getNombreJoueurs() {
        return nombreJoueurs;
    }

    public void setNombreJoueurs(Integer nombreJoueurs) {
        this.nombreJoueurs = nombreJoueurs;
    }

    public boolean isPartiePrivee() {
        return partiePrivee;
    }

    public void setPartiePrivee(boolean partiePrivee) {
        this.partiePrivee = partiePrivee;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }
}

package fr.orleans.m1miage.trioweb.dtos.api;

public class JoinPartieRequestDto {

    private String motDePasse;

    public JoinPartieRequestDto() {
    }

    public JoinPartieRequestDto(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }
}

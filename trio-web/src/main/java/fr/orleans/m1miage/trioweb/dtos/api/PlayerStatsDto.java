package fr.orleans.m1miage.trioweb.dtos.api;

import java.util.List;

public class PlayerStatsDto {
    private String email;
    private String pseudo;
    private long partiesJouees;
    private long victoires;
    private List<VariantStatsDto> variantes;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public long getPartiesJouees() {
        return partiesJouees;
    }

    public void setPartiesJouees(long partiesJouees) {
        this.partiesJouees = partiesJouees;
    }

    public long getVictoires() {
        return victoires;
    }

    public void setVictoires(long victoires) {
        this.victoires = victoires;
    }

    public List<VariantStatsDto> getVariantes() {
        return variantes;
    }

    public void setVariantes(List<VariantStatsDto> variantes) {
        this.variantes = variantes;
    }
}

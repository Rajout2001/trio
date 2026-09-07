package fr.orleans.m1miage.trioweb.dtos.api;

public class VariantStatsDto {
    private String variante;
    private long partiesJouees;
    private long victoires;

    public String getVariante() {
        return variante;
    }

    public void setVariante(String variante) {
        this.variante = variante;
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
}

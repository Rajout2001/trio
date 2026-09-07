package fr.univ.orleans.trioapi.dto.stats;

public class StatistiquesVarianteResponse {
    private final String variante;
    private final long partiesJouees;
    private final long victoires;

    public StatistiquesVarianteResponse(String variante, long partiesJouees, long victoires) {
        this.variante = variante;
        this.partiesJouees = partiesJouees;
        this.victoires = victoires;
    }

    public String getVariante() {
        return variante;
    }

    public long getPartiesJouees() {
        return partiesJouees;
    }

    public long getVictoires() {
        return victoires;
    }
}

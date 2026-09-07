package fr.univ.orleans.trioapi.dto.stats;

import java.util.List;

public class StatistiquesJoueurResponse {
    private final String email;
    private final String pseudo;
    private final long partiesJouees;
    private final long victoires;
    private final List<StatistiquesVarianteResponse> variantes;

    public StatistiquesJoueurResponse(
        String email,
        String pseudo,
        long partiesJouees,
        long victoires,
        List<StatistiquesVarianteResponse> variantes
    ) {
        this.email = email;
        this.pseudo = pseudo;
        this.partiesJouees = partiesJouees;
        this.victoires = victoires;
        this.variantes = variantes;
    }

    public String getEmail() {
        return email;
    }

    public String getPseudo() {
        return pseudo;
    }

    public long getPartiesJouees() {
        return partiesJouees;
    }

    public long getVictoires() {
        return victoires;
    }

    public List<StatistiquesVarianteResponse> getVariantes() {
        return variantes;
    }
}

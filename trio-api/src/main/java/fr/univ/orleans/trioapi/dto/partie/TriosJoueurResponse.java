package fr.univ.orleans.trioapi.dto.partie;

import java.util.List;

public class TriosJoueurResponse {

    private final String joueur;
    private final List<Integer> valeurs;

    public TriosJoueurResponse(String joueur, List<Integer> valeurs) {
        this.joueur = joueur;
        this.valeurs = valeurs;
    }

    public String getJoueur() {
        return joueur;
    }

    public List<Integer> getValeurs() {
        return valeurs;
    }
}

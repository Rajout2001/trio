package fr.univ.orleans.trioapi.dto.partie;

import fr.univ.orleans.trioapi.model.VariantePartie;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CreatePartieRequest {

    @NotNull
    private VariantePartie variante;

    @Min(3)
    @Max(6)
    private int nombreJoueurs;

    private boolean partiePrivee;

    private String motDePasse;

    public VariantePartie getVariante() {
        return variante;
    }

    public void setVariante(VariantePartie variante) {
        this.variante = variante;
    }

    public int getNombreJoueurs() {
        return nombreJoueurs;
    }

    public void setNombreJoueurs(int nombreJoueurs) {
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

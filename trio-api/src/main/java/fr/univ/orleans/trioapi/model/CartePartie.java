package fr.univ.orleans.trioapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cartes_partie")
public class CartePartie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partie_id", nullable = false)
    private Partie partie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proprietaire_id")
    private AppUser proprietaire;

    @Column(nullable = false)
    private int valeur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PositionCarte position;

    protected CartePartie() {
    }

    private CartePartie(Partie partie, AppUser proprietaire, int valeur, PositionCarte position) {
        this.partie = partie;
        this.proprietaire = proprietaire;
        this.valeur = valeur;
        this.position = position;
    }

    public static CartePartie pourMainJoueur(Partie partie, AppUser proprietaire, int valeur) {
        return new CartePartie(partie, proprietaire, valeur, PositionCarte.MAIN_JOUEUR);
    }

    public static CartePartie pourCentreCache(Partie partie, int valeur) {
        return new CartePartie(partie, null, valeur, PositionCarte.CENTRE_CACHE);
    }

    public Long getId() {
        return id;
    }

    public int getValeur() {
        return valeur;
    }

    public AppUser getProprietaire() {
        return proprietaire;
    }

    public PositionCarte getPosition() {
        return position;
    }

    public void revelerDepuisCentre() {
        this.position = PositionCarte.CENTRE_REVELEE;
    }

    public void revelerDepuisMain() {
        this.position = PositionCarte.MAIN_REVELEE;
    }

    public void reCacher() {
        if (this.position == PositionCarte.CENTRE_REVELEE) {
            this.position = PositionCarte.CENTRE_CACHE;
        } else if (this.position == PositionCarte.MAIN_REVELEE) {
            this.position = PositionCarte.MAIN_JOUEUR;
        }
    }

    public void attribuerAuTrio(AppUser gagnant) {
        this.proprietaire = gagnant;
        this.position = PositionCarte.TRIO_GAGNE;
    }
}

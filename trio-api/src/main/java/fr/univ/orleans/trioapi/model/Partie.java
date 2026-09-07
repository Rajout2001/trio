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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "parties")
public class Partie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VariantePartie variante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPartie statut;

    @Column(nullable = false)
    private int nombreJoueursMax;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "createur_id", nullable = false)
    private AppUser createur;

    @Column(nullable = false)
    private boolean distributionEffectuee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "joueur_courant_id")
    private AppUser joueurCourant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gagnant_id")
    private AppUser gagnant;

    @Column(name = "condition_victoire")
    private String conditionVictoire;

    @Column(nullable = false)
    private int numeroTour;

    @Column(name = "dernier_evenement")
    private String dernierEvenement;

    @Column(name = "resolution_echec_apres")
    private Instant resolutionEchecApres;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "joueur_apres_resolution_id")
    private AppUser joueurApresResolution;

    @Column(name = "partie_privee")
    private Boolean partiePrivee;

    @Column(name = "mot_de_passe_hash")
    private String motDePasseHash;

    @ManyToMany
    @JoinTable(
        name = "partie_joueurs",
        joinColumns = @JoinColumn(name = "partie_id"),
        inverseJoinColumns = @JoinColumn(name = "joueur_id")
    )
    private Set<AppUser> joueurs = new LinkedHashSet<>();

    protected Partie() {
    }

    public Partie(VariantePartie variante, int nombreJoueursMax, AppUser createur) {
        this.variante = variante;
        this.statut = StatutPartie.EN_ATTENTE;
        this.nombreJoueursMax = nombreJoueursMax;
        this.createur = createur;
        this.distributionEffectuee = false;
        this.numeroTour = 0;
        this.partiePrivee = false;
        this.joueurs.add(createur);
    }

    public Long getId() {
        return id;
    }

    public VariantePartie getVariante() {
        return variante;
    }

    public StatutPartie getStatut() {
        return statut;
    }

    public int getNombreJoueursMax() {
        return nombreJoueursMax;
    }

    public AppUser getCreateur() {
        return createur;
    }

    public boolean isDistributionEffectuee() {
        return distributionEffectuee;
    }

    public AppUser getJoueurCourant() {
        return joueurCourant;
    }

    public int getNumeroTour() {
        return numeroTour;
    }

    public AppUser getGagnant() {
        return gagnant;
    }

    public String getConditionVictoire() {
        return conditionVictoire;
    }

    public String getDernierEvenement() {
        return dernierEvenement;
    }

    public Instant getResolutionEchecApres() {
        return resolutionEchecApres;
    }

    public AppUser getJoueurApresResolution() {
        return joueurApresResolution;
    }

    public boolean hasEchecEnAttente() {
        return resolutionEchecApres != null && joueurApresResolution != null;
    }

    public boolean isPartiePrivee() {
        return Boolean.TRUE.equals(partiePrivee);
    }

    public String getMotDePasseHash() {
        return motDePasseHash;
    }

    public Set<AppUser> getJoueurs() {
        return joueurs;
    }

    public void proteger(String motDePasseHash) {
        this.partiePrivee = Boolean.TRUE;
        this.motDePasseHash = motDePasseHash;
    }

    public void ajouterJoueur(AppUser joueur) {
        joueurs.add(joueur);
    }

    public int getNombreJoueursActuels() {
        return joueurs.size();
    }

    public boolean estComplete() {
        return getNombreJoueursActuels() >= nombreJoueursMax;
    }

    public boolean estCreateur(Long joueurId) {
        return createur != null && createur.getId().equals(joueurId);
    }

    public boolean contientJoueur(Long joueurId) {
        return joueurs.stream().anyMatch(joueur -> joueur.getId().equals(joueurId));
    }

    public void demarrer(AppUser premierJoueur) {
        this.statut = StatutPartie.EN_COURS;
        this.distributionEffectuee = true;
        this.joueurCourant = premierJoueur;
        this.numeroTour = 1;
        this.dernierEvenement = "La partie commence. Tour de " + premierJoueur.getPseudo() + ".";
    }

    public void passerAuJoueurSuivant(AppUser prochainJoueur) {
        this.joueurCourant = prochainJoueur;
        this.numeroTour += 1;
    }

    public void programmerResolutionEchec(AppUser prochainJoueur, Instant resolutionEchecApres) {
        this.joueurApresResolution = prochainJoueur;
        this.resolutionEchecApres = resolutionEchecApres;
    }

    public void terminerResolutionEchec() {
        this.joueurApresResolution = null;
        this.resolutionEchecApres = null;
    }

    public void terminer(AppUser gagnant, String conditionVictoire) {
        this.statut = StatutPartie.TERMINEE;
        this.joueurCourant = null;
        this.gagnant = gagnant;
        this.conditionVictoire = conditionVictoire;
        this.joueurApresResolution = null;
        this.resolutionEchecApres = null;
    }

    public void noterEvenement(String dernierEvenement) {
        this.dernierEvenement = dernierEvenement;
    }
}

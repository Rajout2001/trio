package fr.orleans.m1miage.trioweb.dtos.api;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class TriosJoueurDto {

    private static final Map<Integer, List<Integer>> LIAISONS_VARIANTE_B = Map.ofEntries(
            Map.entry(1, List.of(6, 8)),
            Map.entry(2, List.of(5, 9)),
            Map.entry(3, List.of(4, 10)),
            Map.entry(4, List.of(3, 11)),
            Map.entry(5, List.of(2, 12)),
            Map.entry(6, List.of(1)),
            Map.entry(7, List.of()),
            Map.entry(8, List.of(1)),
            Map.entry(9, List.of(2)),
            Map.entry(10, List.of(3)),
            Map.entry(11, List.of(4)),
            Map.entry(12, List.of(5))
    );

    private String joueur;
    private List<Integer> valeurs;

    public String getJoueur() { return joueur; }
    public void setJoueur(String joueur) { this.joueur = joueur; }

    public List<Integer> getValeurs() { return valeurs; }
    public void setValeurs(List<Integer> valeurs) { this.valeurs = valeurs; }

    public boolean hasTriosLies() {
        if (valeurs == null || valeurs.size() < 2) {
            return false;
        }
        return valeurs.stream().anyMatch(this::isValeurLiee);
    }

    public boolean isValeurLiee(Integer valeur) {
        if (valeur == null || valeurs == null) {
            return false;
        }
        return LIAISONS_VARIANTE_B.getOrDefault(valeur, List.of()).stream().anyMatch(valeurs::contains);
    }

    public String getTriosLiesLabel() {
        if (valeurs == null) {
            return "";
        }
        return valeurs.stream()
                .filter(this::isValeurLiee)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(" et "));
    }
}

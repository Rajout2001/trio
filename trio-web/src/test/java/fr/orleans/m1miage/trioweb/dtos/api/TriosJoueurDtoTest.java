package fr.orleans.m1miage.trioweb.dtos.api;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TriosJoueurDtoTest {

    @Test
    void hasTriosLies_shouldDetectPrintedLinksWithTwoOptions() {
        TriosJoueurDto trios = new TriosJoueurDto();
        trios.setValeurs(List.of(1, 6));

        assertThat(trios.hasTriosLies()).isTrue();
        assertThat(trios.isValeurLiee(1)).isTrue();
        assertThat(trios.isValeurLiee(6)).isTrue();
        assertThat(trios.getTriosLiesLabel()).isEqualTo("1 et 6");
    }

    @Test
    void hasTriosLies_shouldDetectPrintedLinksWithOneOption() {
        TriosJoueurDto trios = new TriosJoueurDto();
        trios.setValeurs(List.of(12, 5));

        assertThat(trios.hasTriosLies()).isTrue();
        assertThat(trios.getTriosLiesLabel()).isEqualTo("5 et 12");
    }

    @Test
    void hasTriosLies_shouldIgnoreUnlinkedTrios() {
        TriosJoueurDto trios = new TriosJoueurDto();
        trios.setValeurs(List.of(2, 4, 8));

        assertThat(trios.hasTriosLies()).isFalse();
        assertThat(trios.isValeurLiee(2)).isFalse();
        assertThat(trios.isValeurLiee(4)).isFalse();
        assertThat(trios.isValeurLiee(8)).isFalse();
    }
}

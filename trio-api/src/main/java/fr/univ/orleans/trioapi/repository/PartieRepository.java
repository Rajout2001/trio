package fr.univ.orleans.trioapi.repository;

import fr.univ.orleans.trioapi.model.Partie;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PartieRepository extends JpaRepository<Partie, Long> {

    @EntityGraph(attributePaths = {"createur", "joueurs", "joueurCourant", "gagnant"})
    List<Partie> findAllByOrderByIdDesc();

    @Override
    @EntityGraph(attributePaths = {"createur", "joueurs", "joueurCourant", "gagnant"})
    Optional<Partie> findById(Long id);
}

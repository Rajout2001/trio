package fr.univ.orleans.trioapi.repository;

import fr.univ.orleans.trioapi.model.CartePartie;
import fr.univ.orleans.trioapi.model.PositionCarte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CartePartieRepository extends JpaRepository<CartePartie, Long> {

    List<CartePartie> findByPartieId(Long partieId);

    long countByPartieIdAndPosition(Long partieId, PositionCarte position);

    List<CartePartie> findByPartieIdAndProprietaireIdAndPositionOrderByValeurAsc(
        Long partieId,
        Long proprietaireId,
        PositionCarte position
    );

    List<CartePartie> findByPartieIdAndPosition(Long partieId, PositionCarte position);

    Optional<CartePartie> findFirstByPartieIdAndPositionOrderByIdAsc(Long partieId, PositionCarte position);

    Optional<CartePartie> findByIdAndPartieIdAndPosition(Long id, Long partieId, PositionCarte position);

    Optional<CartePartie> findFirstByPartieIdAndProprietaireIdAndPositionOrderByValeurAsc(
        Long partieId,
        Long proprietaireId,
        PositionCarte position
    );

    Optional<CartePartie> findFirstByPartieIdAndProprietaireIdAndPositionOrderByValeurDesc(
        Long partieId,
        Long proprietaireId,
        PositionCarte position
    );

    List<CartePartie> findByPartieIdAndPositionOrderByIdAsc(Long partieId, PositionCarte position);

    List<CartePartie> findByPartieIdAndPositionInOrderByIdAsc(Long partieId, Collection<PositionCarte> positions);

    long countByPartieIdAndProprietaireIdAndPosition(Long partieId, Long proprietaireId, PositionCarte position);
}

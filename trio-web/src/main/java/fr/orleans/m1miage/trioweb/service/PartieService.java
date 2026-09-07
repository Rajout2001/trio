package fr.orleans.m1miage.trioweb.service;

import fr.orleans.m1miage.trioweb.dtos.api.CreatePartieRequestDto;
import fr.orleans.m1miage.trioweb.dtos.api.DistributionDto;
import fr.orleans.m1miage.trioweb.dtos.api.MoveResultDto;
import fr.orleans.m1miage.trioweb.dtos.api.PartieDto;
import fr.orleans.m1miage.trioweb.dtos.api.PlayMoveRequestDto;
import java.util.List;

public interface PartieService {
    List<PartieDto> list();
    List<PartieDto> listFinished();
    PartieDto getById(Long partieId);
    PartieDto create(CreatePartieRequestDto request);
    PartieDto join(Long partieId, String motDePasse);
    PartieDto join(Long partieId);
    PartieDto start(Long partieId);
    DistributionDto getMyDistribution(Long partieId);
    MoveResultDto playMove(Long partieId, PlayMoveRequestDto request);
}

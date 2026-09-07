package fr.orleans.m1miage.trioweb.service;

import fr.orleans.m1miage.trioweb.dtos.api.CreatePartieRequestDto;
import fr.orleans.m1miage.trioweb.dtos.api.DistributionDto;
import fr.orleans.m1miage.trioweb.dtos.api.JoinPartieRequestDto;
import fr.orleans.m1miage.trioweb.dtos.api.MoveResultDto;
import fr.orleans.m1miage.trioweb.dtos.api.PartieDto;
import fr.orleans.m1miage.trioweb.dtos.api.PlayMoveRequestDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

@Service
public class ApiPartieService implements PartieService {

    private final RestClient restClient;

    public ApiPartieService(RestClient trioApiRestClient) {
        this.restClient = trioApiRestClient;
    }

    @Override
    public List<PartieDto> list() {
        PartieDto[] res = restClient.get()
                .uri("/api/parties")
                .retrieve()
                .body(PartieDto[].class);
        return res == null ? List.of() : Arrays.asList(res);
    }

    @Override
    public List<PartieDto> listFinished() {
        PartieDto[] res = restClient.get()
                .uri("/api/parties/terminees")
                .retrieve()
                .body(PartieDto[].class);
        return res == null ? List.of() : Arrays.asList(res);
    }

    @Override
    public PartieDto getById(Long partieId) {
        PartieDto res = restClient.get()
                .uri("/api/parties/{partieId}", partieId)
                .retrieve()
                .body(PartieDto.class);
        if (res == null) throw new IllegalStateException("Reponse API invalide: getById() a retourne null.");
        return res;
    }

    @Override
    public PartieDto create(CreatePartieRequestDto request) {
        PartieDto res = restClient.post()
                .uri("/api/parties")
                .body(request)
                .retrieve()
                .body(PartieDto.class);
        if (res == null) throw new IllegalStateException("Reponse API invalide: create() a retourne null.");
        return res;
    }

    @Override
    public PartieDto join(Long partieId) {
        return join(partieId, null);
    }

    @Override
    public PartieDto join(Long partieId, String motDePasse) {
        PartieDto res = restClient.post()
                .uri("/api/parties/{partieId}/join", partieId)
                .body(new JoinPartieRequestDto(motDePasse))
                .retrieve()
                .body(PartieDto.class);
        if (res == null) throw new IllegalStateException("Reponse API invalide: join() a retourne null.");
        return res;
    }

    @Override
    public PartieDto start(Long partieId) {
        PartieDto res = restClient.post()
                .uri("/api/parties/{partieId}/start", partieId)
                .retrieve()
                .body(PartieDto.class);
        if (res == null) throw new IllegalStateException("Reponse API invalide: start() a retourne null.");
        return res;
    }

    @Override
    public DistributionDto getMyDistribution(Long partieId) {
        DistributionDto res = restClient.get()
                .uri("/api/parties/{partieId}/distribution/me", partieId)
                .retrieve()
                .body(DistributionDto.class);
        if (res == null) throw new IllegalStateException("Reponse API invalide: getMyDistribution() a retourne null.");
        return res;
    }

    @Override
    public MoveResultDto playMove(Long partieId, PlayMoveRequestDto request) {
        MoveResultDto res = restClient.post()
                .uri("/api/parties/{partieId}/moves", partieId)
                .body(request)
                .retrieve()
                .body(MoveResultDto.class);
        if (res == null) throw new IllegalStateException("Reponse API invalide: playMove() a retourne null.");
        return res;
    }

}

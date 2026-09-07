package fr.orleans.m1miage.trioweb.service;

import fr.orleans.m1miage.trioweb.dtos.api.PlayerStatsDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ApiStatsService implements StatsService {

    private final RestClient restClient;

    public ApiStatsService(RestClient trioApiRestClient) {
        this.restClient = trioApiRestClient;
    }

    @Override
    public PlayerStatsDto getMyStats() {
        PlayerStatsDto res = restClient.get()
                .uri("/api/stats/me")
                .retrieve()
                .body(PlayerStatsDto.class);
        if (res == null) {
            throw new IllegalStateException("Reponse API invalide: getMyStats() a retourne null.");
        }
        return res;
    }
}

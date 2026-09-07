package fr.orleans.m1miage.trioweb.service;

import fr.orleans.m1miage.trioweb.dtos.api.AuthResponseDto;
import fr.orleans.m1miage.trioweb.dtos.api.LoginRequestDto;
import fr.orleans.m1miage.trioweb.dtos.api.RegisterRequestDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Locale;

@Service
public class ApiAuthService implements AuthService {

    private final RestClient restClient;

    public ApiAuthService(RestClient trioApiRestClient) {
        this.restClient = trioApiRestClient;
    }

    @Override
    public AuthResponseDto login(String email, String password) {
        try {
            AuthResponseDto res = restClient.post()
                    .uri("/api/auth/login")
                    .body(new LoginRequestDto(email, password))
                    .retrieve()
                    .body(AuthResponseDto.class);

            if (res == null || res.getAccessToken() == null || res.getAccessToken().isBlank()) {
                throw new IllegalStateException("Réponse API invalide (accessToken manquant).");
            }
            return res;

        } catch (RestClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new IllegalArgumentException("Email ou mot de passe incorrect.");
            }
            throw new IllegalStateException("Erreur API: " + e.getStatusCode());
        } catch (RestClientException e) {
            throw new IllegalStateException("Service d'authentification indisponible. Réessaie dans un instant.");
        }
    }

    @Override
    public AuthResponseDto register(String email, String pseudo, String password) {
        try {
            AuthResponseDto res = restClient.post()
                    .uri("/api/auth/register")
                    .body(new RegisterRequestDto(email, pseudo, password))
                    .retrieve()
                    .body(AuthResponseDto.class);

            if (res == null || res.getAccessToken() == null || res.getAccessToken().isBlank()) {
                throw new IllegalStateException("Réponse API invalide (accessToken manquant).");
            }
            return res;

        } catch (RestClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw registrationConflict(e);
            }
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new IllegalArgumentException("Inscription impossible : vérifie les informations saisies.");
            }
            throw new IllegalStateException("Erreur API: " + e.getStatusCode());
        } catch (RestClientException e) {
            throw new IllegalStateException("Service d'authentification indisponible. Réessaie dans un instant.");
        }
    }

    private IllegalArgumentException registrationConflict(RestClientResponseException e) {
        String body = e.getResponseBodyAsString();
        String normalizedBody = body == null ? "" : body.toLowerCase(Locale.ROOT);

        if (normalizedBody.contains("email")) {
            return new IllegalArgumentException("Un compte existe déjà avec cet email.");
        }
        if (normalizedBody.contains("pseudo")) {
            return new IllegalArgumentException("Ce pseudo est déjà utilisé.");
        }
        return new IllegalArgumentException("Inscription impossible : email ou pseudo déjà utilisé.");
    }
}

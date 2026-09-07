package fr.orleans.m1miage.trioweb.service;

import fr.orleans.m1miage.trioweb.dtos.api.AuthResponseDto;

public interface AuthService {
    AuthResponseDto login(String email, String password);
    AuthResponseDto register(String email, String pseudo, String password);
}

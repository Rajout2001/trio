package fr.orleans.m1miage.trioweb.controller;

import fr.orleans.m1miage.trioweb.dtos.api.AuthResponseDto;
import fr.orleans.m1miage.trioweb.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // approche minimal standalone mockMvc juste pour verifier que login retourne le bon token
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService)).build();
    }

    @Test
    void loginSubmit_shouldRedirectToHomeAndStoreJwtInSession_whenLoginIsValid() throws Exception {
        AuthResponseDto auth = authResponse("fake-jwt-token", "player@test.com", "player");

        when(authService.login("player@test.com", "Password123")).thenReturn(auth);

        mockMvc.perform(post("/login")
                        .param("email", "player@test.com")
                        .param("password", "Password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/accueil"))
                .andExpect(request().sessionAttribute(AuthController.SESSION_TOKEN, "fake-jwt-token"))
                .andExpect(request().sessionAttribute(AuthController.SESSION_PSEUDO, "player"));

        verify(authService).login("player@test.com", "Password123");
    }

    @Test
    void loginSubmit_shouldReturnLoginViewWithErrorMessage_whenLoginIsInvalid() throws Exception {
        String errorMessage = "Email ou mot de passe incorrect.";

        when(authService.login("player@test.com", "Password123"))
                .thenThrow(new IllegalArgumentException(errorMessage));

        mockMvc.perform(post("/login")
                        .param("email", "player@test.com")
                        .param("password", "Password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attribute("erreur", errorMessage));

        verify(authService).login("player@test.com", "Password123");
    }

    @Test
    void registerSubmit_shouldReturnRegisterViewWithErrorMessage_whenEmailAlreadyExists() throws Exception {
        String errorMessage = "Un compte existe déjà avec cet email.";

        when(authService.register("player@test.com", "player", "Password123"))
                .thenThrow(new IllegalArgumentException(errorMessage));

        mockMvc.perform(post("/register")
                        .param("email", "player@test.com")
                        .param("pseudo", "player")
                        .param("password", "Password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attribute("erreur", errorMessage));

        verify(authService).register("player@test.com", "player", "Password123");
    }

    private AuthResponseDto authResponse(String token, String email, String pseudo) {
        AuthResponseDto auth = new AuthResponseDto();
        auth.setAccessToken(token);
        auth.setEmail(email);
        auth.setPseudo(pseudo);
        return auth;
    }
}

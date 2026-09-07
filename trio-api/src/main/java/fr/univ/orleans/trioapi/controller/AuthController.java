package fr.univ.orleans.trioapi.controller;

import fr.univ.orleans.trioapi.dto.auth.AuthResponse;
import fr.univ.orleans.trioapi.dto.auth.LoginRequest;
import fr.univ.orleans.trioapi.dto.auth.LogoutRequest;
import fr.univ.orleans.trioapi.dto.auth.MessageResponse;
import fr.univ.orleans.trioapi.dto.auth.RefreshTokenRequest;
import fr.univ.orleans.trioapi.dto.auth.RegisterRequest;
import fr.univ.orleans.trioapi.dto.auth.UpdateCredentialsRequest;
import fr.univ.orleans.trioapi.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Create a user account")
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @Operation(summary = "Authenticate user and return JWT")
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "Refresh access token using refresh token")
    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request.getRefreshToken());
    }

    @Operation(summary = "Update authenticated user email and/or password")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/me")
    public MessageResponse updateCredentials(
        @Valid @RequestBody UpdateCredentialsRequest request,
        Authentication authentication
    ) {
        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Authentication is required");
        }
        authService.updateCredentials(authentication.getName(), request);
        return new MessageResponse("Credentials updated");
    }

    @Operation(summary = "Logout current session and revoke provided tokens")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public MessageResponse logout(
        @RequestBody(required = false) LogoutRequest request,
        Authentication authentication,
        @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Authentication is required");
        }

        String accessToken = extractBearerToken(authorizationHeader);
        String refreshToken = request == null ? null : request.getRefreshToken();
        authService.logout(accessToken, refreshToken);
        return new MessageResponse("Logged out");
    }

    private String extractBearerToken(String header) {
        if (header == null) {
            return null;
        }
        String prefix = "Bearer ";
        if (!header.startsWith(prefix)) {
            return null;
        }
        return header.substring(prefix.length());
    }
}

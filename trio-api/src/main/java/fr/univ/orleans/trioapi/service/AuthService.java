package fr.univ.orleans.trioapi.service;

import fr.univ.orleans.trioapi.dto.auth.AuthResponse;
import fr.univ.orleans.trioapi.dto.auth.LoginRequest;
import fr.univ.orleans.trioapi.dto.auth.RegisterRequest;
import fr.univ.orleans.trioapi.dto.auth.UpdateCredentialsRequest;
import fr.univ.orleans.trioapi.model.AppUser;
import fr.univ.orleans.trioapi.repository.AppUserRepository;
import fr.univ.orleans.trioapi.security.JwtService;
import fr.univ.orleans.trioapi.security.TokenRegistryService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final TokenRegistryService tokenRegistryService;

    public AuthService(
        AppUserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AppUserDetailsService userDetailsService,
        JwtService jwtService,
        TokenRegistryService tokenRegistryService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.tokenRegistryService = tokenRegistryService;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        String pseudo = normalizePseudo(request.getPseudo());

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(CONFLICT, "Email already in use");
        }
        if (userRepository.existsByPseudoIgnoreCase(pseudo)) {
            throw new ResponseStatusException(CONFLICT, "Pseudo already in use");
        }

        AppUser user = new AppUser(email, pseudo, passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        return issueTokens(user);
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        AppUser user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return issueTokens(user);
    }

    public AuthResponse refresh(String refreshToken) {
        // Upgrade Backend Imen — renouvelle l’access token depuis un refresh token valide.
        String token = refreshToken == null ? null : refreshToken.trim();
        if (!StringUtils.hasText(token)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Refresh token is required");
        }

        String email;
        try {
            if (!jwtService.isRefreshToken(token)) {
                throw new ResponseStatusException(UNAUTHORIZED, "Invalid refresh token");
            }
            email = normalizeEmail(jwtService.extractUsername(token));
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid refresh token");
        }

        if (!tokenRegistryService.isRefreshTokenActive(token, email)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Refresh token is revoked or expired");
        }

        AppUser user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        tokenRegistryService.revokeRefreshToken(token);
        return issueTokens(user);
    }

    public void logout(String accessToken, String refreshToken) {
        // Upgrade Backend Imen — révoque les jetons fournis lors de la déconnexion.
        if (StringUtils.hasText(accessToken)) {
            String token = accessToken.trim();
            try {
                long expiresAt = jwtService.extractExpiration(token).getTime();
                tokenRegistryService.revokeAccessToken(token, expiresAt);
            } catch (Exception ignored) {
                // Ignore malformed access tokens on logout.
            }
        }

        if (StringUtils.hasText(refreshToken)) {
            tokenRegistryService.revokeRefreshToken(refreshToken.trim());
        }
    }

    public void updateCredentials(String authenticatedEmail, UpdateCredentialsRequest request) {
        AppUser user = userRepository.findByEmail(normalizeEmail(authenticatedEmail))
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Current password is invalid");
        }

        boolean hasEmailUpdate = StringUtils.hasText(request.getNewEmail());
        boolean hasPasswordUpdate = StringUtils.hasText(request.getNewPassword());
        if (!hasEmailUpdate && !hasPasswordUpdate) {
            throw new ResponseStatusException(BAD_REQUEST, "Provide newEmail and/or newPassword");
        }

        if (hasEmailUpdate) {
            String newEmail = normalizeEmail(request.getNewEmail());
            if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new ResponseStatusException(CONFLICT, "Email already in use");
            }
            user.setEmail(newEmail);
        }

        if (hasPasswordUpdate) {
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        }

        userRepository.save(user);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String normalizePseudo(String pseudo) {
        return pseudo == null ? null : pseudo.trim();
    }

    private AuthResponse issueTokens(AppUser user) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        tokenRegistryService.registerRefreshToken(
            refreshToken,
            user.getEmail(),
            jwtService.extractExpiration(refreshToken).getTime()
        );
        return new AuthResponse(
            accessToken,
            jwtService.getExpirationMs(),
            refreshToken,
            jwtService.getRefreshExpirationMs(),
            user.getEmail(),
            user.getPseudo()
        );
    }
}

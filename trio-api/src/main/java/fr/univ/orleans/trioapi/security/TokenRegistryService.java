package fr.univ.orleans.trioapi.security;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Upgrade Backend Imen — centralise les jetons actifs et révoqués en mémoire. */
@Service
public class TokenRegistryService {

    private final ConcurrentMap<String, Long> revokedAccessTokens = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, RefreshTokenEntry> activeRefreshTokens = new ConcurrentHashMap<>();

    public void registerRefreshToken(String refreshToken, String email, long expiresAtEpochMs) {
        activeRefreshTokens.put(refreshToken, new RefreshTokenEntry(email, expiresAtEpochMs));
    }

    public boolean isRefreshTokenActive(String refreshToken, String email) {
        RefreshTokenEntry entry = activeRefreshTokens.get(refreshToken);
        if (entry == null) {
            return false;
        }
        if (entry.expiresAtEpochMs() <= System.currentTimeMillis()) {
            activeRefreshTokens.remove(refreshToken);
            return false;
        }
        return entry.email().equals(email);
    }

    public void revokeRefreshToken(String refreshToken) {
        activeRefreshTokens.remove(refreshToken);
    }

    public void revokeAccessToken(String accessToken, long expiresAtEpochMs) {
        revokedAccessTokens.put(accessToken, expiresAtEpochMs);
    }

    public boolean isAccessTokenRevoked(String accessToken) {
        Long expiresAt = revokedAccessTokens.get(accessToken);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt <= System.currentTimeMillis()) {
            revokedAccessTokens.remove(accessToken);
            return false;
        }
        return true;
    }

    private record RefreshTokenEntry(String email, long expiresAtEpochMs) {
    }
}

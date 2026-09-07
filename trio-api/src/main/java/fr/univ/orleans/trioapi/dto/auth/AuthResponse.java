package fr.univ.orleans.trioapi.dto.auth;

public class AuthResponse {

    private final String tokenType = "Bearer";
    private final String accessToken;
    private final long expiresIn;
    private final String refreshToken;
    private final long refreshExpiresIn;
    private final String email;
    private final String pseudo;

    public AuthResponse(String accessToken, long expiresIn, String email, String pseudo) {
        this(accessToken, expiresIn, null, 0, email, pseudo);
    }

    public AuthResponse(
        String accessToken,
        long expiresIn,
        String refreshToken,
        long refreshExpiresIn,
        String email,
        String pseudo
    ) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.refreshToken = refreshToken;
        this.refreshExpiresIn = refreshExpiresIn;
        this.email = email;
        this.pseudo = pseudo;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getRefreshExpiresIn() {
        return refreshExpiresIn;
    }

    public String getEmail() {
        return email;
    }

    public String getPseudo() {
        return pseudo;
    }
}

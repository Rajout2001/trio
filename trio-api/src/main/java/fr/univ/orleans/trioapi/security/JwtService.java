package fr.univ.orleans.trioapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final SecretKey signingKey;
    private final long expirationMs;
    private final long refreshExpirationMs;

    public JwtService(
        @Value("${jwt.secret}") String jwtSecret,
        @Value("${jwt.expiration-ms}") long expirationMs,
        @Value("${jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs
    ) {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generateToken(UserDetails userDetails) {
        return generateAccessToken(userDetails);
    }

    // Upgrade Backend Imen — distingue les access tokens des refresh tokens.
    public String generateAccessToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
            .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
            .subject(userDetails.getUsername())
            .issuedAt(now)
            .expiration(expiry)
            .signWith(signingKey)
            .compact();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshExpirationMs);

        return Jwts.builder()
            .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
            .subject(userDetails.getUsername())
            .issuedAt(now)
            .expiration(expiry)
            .signWith(signingKey)
            .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && isAccessToken(token) && !isTokenExpired(token);
    }

    public boolean isAccessToken(String token) {
        String tokenType = extractAllClaims(token).get(TOKEN_TYPE_CLAIM, String.class);
        return tokenType == null || ACCESS_TOKEN_TYPE.equals(tokenType);
    }

    public boolean isRefreshToken(String token) {
        String tokenType = extractAllClaims(token).get(TOKEN_TYPE_CLAIM, String.class);
        return REFRESH_TOKEN_TYPE.equals(tokenType);
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}

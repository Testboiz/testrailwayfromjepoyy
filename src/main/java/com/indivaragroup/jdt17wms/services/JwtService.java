package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    // Token type constants
    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    // Minimum 256-bit key for HS256
    private static final String DEFAULT_SECRET = "indivaragroupwmsjsonwebtokensecretkey2026supersecretkey";

    @Value("${jwt.secret:" + DEFAULT_SECRET + "}")
    private String secretKey;

    @Value("${jwt.access-token-expiration-ms:900000}") // 15 minutes
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token-expiration-ms:604800000}") // 7 days
    private long refreshTokenExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        return buildToken(user.getEmail(), accessTokenExpirationMs, ACCESS_TOKEN_TYPE);
    }

    public String generateRefreshToken(User user) {
        return buildToken(user.getEmail(), refreshTokenExpirationMs, REFRESH_TOKEN_TYPE);
    }

    private String buildToken(String email, long expirationMs, String tokenType) {
        return Jwts.builder()
                .subject(email)
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String getEmailFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, User user) {
        final String email = getEmailFromToken(token);
        return (email.equals(user.getEmail())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return getExpiration(token).before(new Date());
    }

    private Date getExpiration(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getTokenType(String token) {
        return getClaim(token, claims -> claims.get(TOKEN_TYPE_CLAIM, String.class));
    }

    public boolean isAccessToken(String token) {
        return ACCESS_TOKEN_TYPE.equals(getTokenType(token));
    }

    public boolean isRefreshToken(String token) {
        return REFRESH_TOKEN_TYPE.equals(getTokenType(token));
    }
}

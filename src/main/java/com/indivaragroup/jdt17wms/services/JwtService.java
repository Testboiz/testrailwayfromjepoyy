package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.function.Function;

@Service
public class JwtService {

    // Token type constants
    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    // Claim name constants
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_IAT = "iat";
    private static final String CLAIM_EXP = "exp";

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
        return buildToken(user, accessTokenExpirationMs, ACCESS_TOKEN_TYPE);
    }

    public String generateRefreshToken(User user) {
        return buildToken(user, refreshTokenExpirationMs, REFRESH_TOKEN_TYPE);
    }

    private String buildToken(User user, long expirationMs, String tokenType) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_USER_ID, user.getId().toString())
                .claim(CLAIM_ROLE, "ROLE_" + user.getRole().name())
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .claim(CLAIM_IAT, now.getEpochSecond())
                .claim(CLAIM_EXP, now.plusMillis(expirationMs).getEpochSecond())
                .signWith(getSigningKey())
                .compact();
    }

    public String getRoleFromToken(String token) {
        return getClaim(token, claims -> claims.get(CLAIM_ROLE, String.class));
    }

    public String getUserIdFromToken(String token) {
        return getClaim(token, claims -> claims.get(CLAIM_USER_ID, String.class));
    }

    public String getEmailFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public String getEmailClaimFromToken(String token) {
        try {
            return getClaim(token, claims -> claims.get(CLAIM_EMAIL, String.class));
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return e.getClaims().get(CLAIM_EMAIL, String.class);
        }
    }

    public String getUserIdClaimFromToken(String token) {
        try {
            return getClaim(token, claims -> claims.get(CLAIM_USER_ID, String.class));
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return e.getClaims().get(CLAIM_USER_ID, String.class);
        }
    }

    public boolean isTokenValid(String token, User user) {
        try {
            final String email = getEmailFromToken(token);
            return (email.equals(user.getEmail()));
        } catch (Exception e) {
            return false;
        }
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

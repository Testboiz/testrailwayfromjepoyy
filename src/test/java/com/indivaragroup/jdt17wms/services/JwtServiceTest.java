package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.models.User;
import com.indivaragroup.jdt17wms.models.enums.UserRole;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Set secret and expirations via reflection
        ReflectionTestUtils.setField(jwtService, "secretKey", "indivaragroupwmsjsonwebtokensecretkey2026supersecretkey");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpirationMs", 900000);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpirationMs", 604800000);
    }

    @Test
    void generateAccessToken_shouldContainCorrectClaims() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .name("John Doe")
                .email("john@example.com")
                .role(UserRole.USER)
                .build();
        String token = jwtService.generateAccessToken(user);
        assertNotNull(token);
        assertTrue(jwtService.isAccessToken(token));
        assertFalse(jwtService.isRefreshToken(token));
        assertEquals("john@example.com", jwtService.getEmailFromToken(token));
        assertEquals("USER", jwtService.getRoleFromToken(token));
        assertEquals(userId.toString(), jwtService.getUserIdFromToken(token));
        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void generateRefreshToken_shouldContainCorrectClaims() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .name("Admin User")
                .email("admin@example.com")
                .role(UserRole.ADMIN)
                .build();
        String token = jwtService.generateRefreshToken(user);
        assertNotNull(token);
        assertFalse(jwtService.isAccessToken(token));
        assertTrue(jwtService.isRefreshToken(token));
        assertEquals("admin@example.com", jwtService.getEmailFromToken(token));
        assertEquals("ADMIN", jwtService.getRoleFromToken(token));
        assertEquals(userId.toString(), jwtService.getUserIdFromToken(token));
    }

    @Test
    void getEmailClaimFromToken_expiredToken_returnsEmailClaim() {
        String secret = "indivaragroupwmsjsonwebtokensecretkey2026supersecretkey";
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        String expiredToken = Jwts.builder()
                .subject("expired@example.com")
                .claim("email", "expired@example.com")
                .claim("userId", UUID.randomUUID().toString())
                .claim("role", "USER")
                .claim("token_type", "access")
                .claim("iat", now.minusSeconds(20).getEpochSecond())
                .claim("exp", now.minusSeconds(10).getEpochSecond())
                .signWith(key)
                .compact();
        assertEquals("expired@example.com", jwtService.getEmailFromToken(expiredToken));
    }

    @Test
    void getUserIdClaimFromToken_expiredToken_returnsUserIdClaim() {
        String secret = "indivaragroupwmsjsonwebtokensecretkey2026supersecretkey";
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        UUID uid = UUID.randomUUID();
        Instant now = Instant.now();
        String expiredToken = Jwts.builder()
                .subject("user@example.com")
                .claim("email", "user@example.com")
                .claim("userId", uid.toString())
                .claim("role", "USER")
                .claim("token_type", "access")
                .claim("iat", now.minusSeconds(20).getEpochSecond())
                .claim("exp", now.minusSeconds(10).getEpochSecond())
                .signWith(key)
                .compact();
        assertEquals(uid.toString(), jwtService.getUserIdFromToken(expiredToken));
    }

    @Test
    void isTokenValid_mismatchedEmail_returnsFalse() {
        UUID userId = UUID.randomUUID();
        User tokenUser = User.builder()
                .id(userId)
                .name("Alice")
                .email("alice@example.com")
                .role(UserRole.USER)
                .build();
        User differentUser = User.builder()
                .id(userId)
                .name("Bob")
                .email("bob@example.com")
                .role(UserRole.USER)
                .build();
        String token = jwtService.generateAccessToken(tokenUser);
        assertFalse(jwtService.isTokenValid(token, differentUser));
    }

    @Test
    void isAccessToken_refreshToken_returnsFalse() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .name("Carol")
                .email("carol@example.com")
                .role(UserRole.USER)
                .build();
        String refreshToken = jwtService.generateRefreshToken(user);
        assertFalse(jwtService.isAccessToken(refreshToken));
        assertTrue(jwtService.isRefreshToken(refreshToken));
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .name("Dave")
                .email("dave@example.com")
                .role(UserRole.USER)
                .build();
        // create expired token
        String secret = "indivaragroupwmsjsonwebtokensecretkey2026supersecretkey";
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        String expiredToken = Jwts.builder()
                .subject(user.getEmail())
                .claim("email", user.getEmail())
                .claim("userId", user.getId().toString())
                .claim("role", "USER")
                .claim("token_type", "access")
                .claim("iat", now.minusSeconds(20).getEpochSecond())
                .claim("exp", now.minusSeconds(10).getEpochSecond())
                .signWith(key)
                .compact();
        assertFalse(jwtService.isTokenValid(expiredToken, user));
    }
}

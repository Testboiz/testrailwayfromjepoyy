package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.models.User;
import com.indivaragroup.jdt17wms.models.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Set the private fields using ReflectionTestUtils
        ReflectionTestUtils.setField(jwtService, "secretKey", "indivaragroupwmsjsonwebtokensecretkey2026supersecretkey");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpirationMs", 900000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpirationMs", 604800000L);
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
        assertEquals("ROLE_USER", jwtService.getRoleFromToken(token));
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
        assertEquals("ROLE_ADMIN", jwtService.getRoleFromToken(token));
        assertEquals(userId.toString(), jwtService.getUserIdFromToken(token));
    }
}

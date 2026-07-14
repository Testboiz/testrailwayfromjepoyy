package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.dto.request.AuthDTO;
import com.indivaragroup.jdt17wms.dto.response.AuthSuccessDTO;
import com.indivaragroup.jdt17wms.dto.response.LogoutSuccessDTO;
import com.indivaragroup.jdt17wms.dto.response.RefreshTokenSuccessDTO;
import com.indivaragroup.jdt17wms.dto.utils.UserSecurityProjection;
import com.indivaragroup.jdt17wms.exceptions.BadRequestException;
import com.indivaragroup.jdt17wms.exceptions.InvalidTokenException;
import com.indivaragroup.jdt17wms.exceptions.UnauthorizedException;
import com.indivaragroup.jdt17wms.models.AuditLog;
import com.indivaragroup.jdt17wms.models.User;
import com.indivaragroup.jdt17wms.models.enums.UserRole;
import com.indivaragroup.jdt17wms.repositories.AuditLogRepository;
import com.indivaragroup.jdt17wms.repositories.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuthService authService;

    // --- Token Extraction Tests ---

    @Test
    void extractEmailFromToken_shouldReturnEmail() {
        when(jwtService.getEmailClaimFromToken("token")).thenReturn("user@example.com");
        assertEquals("user@example.com", authService.extractEmailFromToken("token"));
    }

    @Test
    void extractUserIdFromToken_withValidUserId_shouldReturnUUID() {
        UUID userId = UUID.randomUUID();
        when(jwtService.getUserIdClaimFromToken("token")).thenReturn(userId.toString());
        assertEquals(userId, authService.extractUserIdFromToken("token"));
    }

    @Test
    void extractUserIdFromToken_withNullUserId_shouldReturnNull() {
        when(jwtService.getUserIdClaimFromToken("token")).thenReturn(null);
        assertNull(authService.extractUserIdFromToken("token"));
    }

    // --- Login Tests ---

    @Test
    void login_withNullEmail_shouldThrowBadRequestException() {
        AuthDTO dto = new AuthDTO(null, null, "Password123!");
        assertThrows(BadRequestException.class, () -> authService.login(dto));
    }

    @Test
    void login_withUserNotFound_shouldThrowBadRequestException() {
        AuthDTO dto = new AuthDTO(null, "notfound@example.com", "Password123!");
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.login(dto));
        assertEquals("Email Or Password Invalid", ex.getMessage());
    }

    @Test
    void login_withPasswordMismatch_shouldThrowBadRequestException() {
        AuthDTO dto = new AuthDTO(null, "user@example.com", "WrongPassword");
        User user = User.builder().email("user@example.com").passwordHash("hash").build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "hash")).thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.login(dto));
        assertEquals("Email or Password Invalid", ex.getMessage());
    }

    @Test
    void login_success_firstUser_shouldReturnAdminSuccessResponse() {
        AuthDTO dto = new AuthDTO(null, "user@example.com", "Password123!");
        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .email("user@example.com")
                .passwordHash("hash")
                .role(UserRole.USER)
                .questionnaireCompleted(false)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "hash")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");

        UserSecurityProjection projection = mock(UserSecurityProjection.class);
        when(projection.getPriorCount()).thenReturn(0L); // first user (priorCount is 0)
        when(userRepository.findUserSecurityProjectionByEmail("user@example.com")).thenReturn(Optional.of(projection));

        AuthSuccessDTO response = authService.login(dto);

        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals("Login successful", response.getMessage());
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertTrue(response.getUser().getIsAdmin());

        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void login_success_notFirstUser_userRoleUser_shouldReturnUserSuccessResponse() {
        AuthDTO dto = new AuthDTO(null, "user@example.com", "Password123!");
        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .email("user@example.com")
                .passwordHash("hash")
                .role(UserRole.USER)
                .questionnaireCompleted(false)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "hash")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");

        UserSecurityProjection projection = mock(UserSecurityProjection.class);
        when(projection.getPriorCount()).thenReturn(1L); // not first user
        when(userRepository.findUserSecurityProjectionByEmail("user@example.com")).thenReturn(Optional.of(projection));

        AuthSuccessDTO response = authService.login(dto);

        assertNotNull(response);
        assertFalse(response.getUser().getIsAdmin());
    }

    @Test
    void login_success_notFirstUser_userRoleAdmin_shouldReturnAdminSuccessResponse() {
        AuthDTO dto = new AuthDTO(null, "admin@example.com", "Password123!");
        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Admin User")
                .email("admin@example.com")
                .passwordHash("hash")
                .role(UserRole.ADMIN)
                .questionnaireCompleted(false)
                .build();

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "hash")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");

        UserSecurityProjection projection = mock(UserSecurityProjection.class);
        when(projection.getPriorCount()).thenReturn(1L); // not first user, but role is ADMIN
        when(userRepository.findUserSecurityProjectionByEmail("admin@example.com")).thenReturn(Optional.of(projection));

        AuthSuccessDTO response = authService.login(dto);

        assertNotNull(response);
        assertTrue(response.getUser().getIsAdmin());
    }

    @Test
    void login_success_projectionNotFound_shouldDefaultToFalseAdmin() {
        AuthDTO dto = new AuthDTO(null, "user@example.com", "Password123!");
        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .email("user@example.com")
                .passwordHash("hash")
                .role(UserRole.USER)
                .questionnaireCompleted(false)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "hash")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");
        when(userRepository.findUserSecurityProjectionByEmail("user@example.com")).thenReturn(Optional.empty());

        AuthSuccessDTO response = authService.login(dto);

        assertNotNull(response);
        assertFalse(response.getUser().getIsAdmin());
    }

    // --- Logout Tests ---

    @Test
    void logout_success_shouldSaveAuditLogAndReturnSuccess() {
        UUID userId = UUID.randomUUID();
        LogoutSuccessDTO response = authService.logout(userId, "user@example.com");

        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals("Logout successful", response.getMessage());

        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void logout_withNullEmail_shouldSaveAuditLogWithAnonymousAndReturnSuccess() {
        UUID userId = UUID.randomUUID();
        LogoutSuccessDTO response = authService.logout(userId, null);

        assertNotNull(response);
        assertTrue(response.getSuccess());

        verify(auditLogRepository, times(1)).save(argThat(audit -> "anonymous".equals(audit.getUserName())));
    }

    // --- Refresh Token Tests ---

    @Test
    void refreshToken_withNullOrEmptyToken_shouldThrowBadRequestException() {
        assertThrows(BadRequestException.class, () -> authService.refreshToken(null));
        assertThrows(BadRequestException.class, () -> authService.refreshToken("   "));
    }

    @Test
    void refreshToken_withInvalidTokenType_shouldThrowInvalidTokenException() {
        when(jwtService.isRefreshToken("bad-token")).thenReturn(false);
        assertThrows(InvalidTokenException.class, () -> authService.refreshToken("bad-token"));
    }

    @Test
    void refreshToken_withExpiredToken_shouldThrowUnauthorizedException() {
        when(jwtService.isRefreshToken("expired-token")).thenReturn(true);
        ExpiredJwtException expiredEx = mock(ExpiredJwtException.class);
        when(jwtService.getEmailFromToken("expired-token")).thenThrow(expiredEx);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> authService.refreshToken("expired-token"));
        assertEquals("Refresh token expired", ex.getMessage());
    }

    @Test
    void refreshToken_withInvalidTokenSignature_shouldThrowInvalidTokenException() {
        when(jwtService.isRefreshToken("invalid-sig-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("invalid-sig-token")).thenThrow(new InvalidTokenException("Invalid signature"));

        InvalidTokenException ex = assertThrows(InvalidTokenException.class, () -> authService.refreshToken("invalid-sig-token"));
        assertEquals("Invalid signature", ex.getMessage());
    }

    @Test
    void refreshToken_withGeneralException_shouldThrowInvalidTokenException() {
        when(jwtService.isRefreshToken("error-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("error-token")).thenThrow(new RuntimeException("Unknown error"));

        InvalidTokenException ex = assertThrows(InvalidTokenException.class, () -> authService.refreshToken("error-token"));
        assertEquals("Invalid refresh token", ex.getMessage());
    }

    @Test
    void refreshToken_withUserNotFound_shouldThrowUnauthorizedException() {
        when(jwtService.isRefreshToken("valid-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("valid-token")).thenReturn("notfound@example.com");
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> authService.refreshToken("valid-token"));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void refreshToken_success_shouldReturnNewTokens() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .email("user@example.com")
                .build();

        when(jwtService.isRefreshToken("valid-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("valid-token")).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("new-access");
        when(jwtService.generateRefreshToken(user)).thenReturn("new-refresh");

        RefreshTokenSuccessDTO response = authService.refreshToken("valid-token");

        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals("Token refreshed successfully", response.getMessage());
        assertEquals("new-access", response.getAccessToken());
        assertEquals("new-refresh", response.getRefreshToken());

        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }
}

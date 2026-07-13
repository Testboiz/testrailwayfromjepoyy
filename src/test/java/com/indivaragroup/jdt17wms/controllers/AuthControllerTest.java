package com.indivaragroup.jdt17wms.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indivaragroup.jdt17wms.dto.request.AuthDTO;
import com.indivaragroup.jdt17wms.dto.response.AuthSuccessDTO;
import com.indivaragroup.jdt17wms.dto.response.LogoutSuccessDTO;
import com.indivaragroup.jdt17wms.dto.response.UserDTO;
import com.indivaragroup.jdt17wms.services.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private final UserDTO mockUser = UserDTO.builder()
            .id(UUID.randomUUID())
            .email("test@example.com")
            .isAdmin(false)
            .build();

    // ✅ LOGIN — success
    @Test
    void login_withValidCredentials_shouldReturnToken() throws Exception {
        AuthSuccessDTO mockResponse = AuthSuccessDTO.builder()
                .success(true)
                .message("Login successful")
                .accessToken("eyJhbGciOiJIUzI1NiJ9.test")
                .expiresIn(900)
                .user(mockUser)
                .build();

        when(authService.login(any(AuthDTO.class))).thenReturn(mockResponse);

        String body = objectMapper.writeValueAsString(new AuthDTO("Test", "test@example.com", "Test1234!"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.accessToken").value("eyJhbGciOiJIUzI1NiJ9.test"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }

    // ✅ LOGOUT — with Bearer token (controller extracts email via authService.extractEmailFromToken)
    @Test
    void logout_withValidToken_shouldReturnSuccess() throws Exception {
        when(authService.extractEmailFromToken(anyString())).thenReturn("test@example.com");
        when(authService.extractUserIdFromToken(anyString())).thenReturn(UUID.randomUUID());

        LogoutSuccessDTO mockResponse = LogoutSuccessDTO.builder()
                .success(true)
                .message("Logout successful")
                .build();

        when(authService.logout(any(UUID.class), anyString())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }

    // ❌ LOGOUT — without token should fail
    @Test
    void logout_withoutToken_shouldFailWithUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("No token provided"));
    }
}

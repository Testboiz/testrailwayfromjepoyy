package com.indivaragroup.jdt17wms.controllers;

import com.indivaragroup.jdt17wms.dto.request.AuthDTO;
import com.indivaragroup.jdt17wms.dto.request.RefreshTokenDTO;
import com.indivaragroup.jdt17wms.dto.response.AuthSuccessDTO;
import com.indivaragroup.jdt17wms.dto.response.LogoutSuccessDTO;
import com.indivaragroup.jdt17wms.dto.response.RefreshTokenSuccessDTO;
import com.indivaragroup.jdt17wms.exceptions.BadRequestException;
import com.indivaragroup.jdt17wms.services.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthSuccessDTO login(@RequestBody(required = false) AuthDTO dto) {
        if (dto == null){
            throw new BadRequestException("Invalid Request Body");
        }
        return authService.login(dto);
    }

    @PostMapping("/register")
    public AuthSuccessDTO register(@RequestBody(required = false) AuthDTO dto) {
        if (dto == null) {
            throw new BadRequestException("Invalid Request Body");
        }
        return authService.register(dto);
    }

    // 🚪 LOGOUT
    @PostMapping("/logout")
    public LogoutSuccessDTO logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String email = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                email = authService.extractEmailFromToken(token);
            } catch (Exception ignored) {}
        }
        return authService.logout(email);
    }

    // 🔄 REFRESH TOKEN
    @PostMapping("/refresh")
    public RefreshTokenSuccessDTO refresh(@RequestBody(required = false) RefreshTokenDTO dto) {
        if (dto == null || dto.getRefreshToken() == null || dto.getRefreshToken().trim().isEmpty()) {
            throw new BadRequestException("Refresh token is required");
        }
        return authService.refreshToken(dto.getRefreshToken());
    }
}

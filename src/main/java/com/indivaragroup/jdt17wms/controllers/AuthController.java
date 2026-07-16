package com.indivaragroup.jdt17wms.controllers;
import java.util.UUID;

import com.indivaragroup.jdt17wms.dto.request.LoginDTO;
import com.indivaragroup.jdt17wms.dto.request.RefreshTokenDTO;
import com.indivaragroup.jdt17wms.dto.request.RegisterDTO;
import com.indivaragroup.jdt17wms.dto.response.ApiPath;
import com.indivaragroup.jdt17wms.dto.response.ApiResponse;
import com.indivaragroup.jdt17wms.dto.response.auth.AuthSuccessDTO;
import com.indivaragroup.jdt17wms.dto.response.auth.LogoutSuccessDTO;
import com.indivaragroup.jdt17wms.dto.response.auth.RefreshTokenSuccessDTO;
import com.indivaragroup.jdt17wms.dto.utils.ApiError;
import com.indivaragroup.jdt17wms.dto.utils.ApiSuccess;
import com.indivaragroup.jdt17wms.exceptions.CoreThrowHandler;
import com.indivaragroup.jdt17wms.services.AuthService;
import com.indivaragroup.jdt17wms.services.JwtService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(ApiPath.BASE_AUTH_PATH)
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);


    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping(ApiPath.LOGIN_PATH)
    public ApiResponse<AuthSuccessDTO> login(@Valid @RequestBody LoginDTO dto) {
        return ApiResponse.success(ApiSuccess.LOGIN, authService.login(dto));
    }

    @PostMapping(ApiPath.REGISTER_PATH)
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterDTO dto) {
        authService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(ApiSuccess.REGISTER, null));
    }

    @PostMapping(ApiPath.LOGOUT_PATH)
    public ApiResponse<LogoutSuccessDTO> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {

        String email = null;
        UUID userId = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                email = jwtService.getEmailFromToken(token);
                userId = jwtService.getUserIdFromToken(token);
            } catch (Exception e) {
                log.debug("Logout token parse failed {}", e.getMessage());
            }
        }
        return ApiResponse.success(ApiSuccess.LOGOUT, authService.logout(email, userId));
    }

    @PostMapping(ApiPath.REFRESH_TOKEN_PATH)
    public ApiResponse<RefreshTokenSuccessDTO> refresh(@Valid @RequestBody RefreshTokenDTO dto) {
        return ApiResponse.success(ApiSuccess.REFRESH_TOKEN,
                authService.refreshToken(dto.getRefreshToken()));
    }
}

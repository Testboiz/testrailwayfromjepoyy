package com.indivaragroup.jdt17wms.controllers;

import com.indivaragroup.jdt17wms.services.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
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
    public void login() {
    }

    @PostMapping("/register")
    public void register() {
    }

    @PostMapping("/logout")
    public void logout() {
    }
}

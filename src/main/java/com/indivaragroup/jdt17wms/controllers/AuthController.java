package com.indivaragroup.jdt17wms.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

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

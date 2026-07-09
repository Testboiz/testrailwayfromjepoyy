package com.indivaragroup.jdt17wms.controllers;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @GetMapping
    public void getAllUsers() {
    }

    @PutMapping
    public void updateUser() {
    }
}

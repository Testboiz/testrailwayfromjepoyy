package com.indivaragroup.jdt17wms.controllers;

import com.indivaragroup.jdt17wms.services.UserManagementService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserManagementService userManagementService;

    public UserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    public void getAllUsers() {
    }

    @PutMapping
    public void updateUser() {
    }
}

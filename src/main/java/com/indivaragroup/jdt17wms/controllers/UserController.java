package com.indivaragroup.jdt17wms.controllers;

import com.indivaragroup.jdt17wms.dto.request.AdminUserAccessDTO;
import com.indivaragroup.jdt17wms.models.User;
import com.indivaragroup.jdt17wms.services.UserManagementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class UserController {

    private final UserManagementService userManagementService;

    public UserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping("/api/v1/users")
    public Page<User> getAllUsers(Pageable pageable) {
        return userManagementService.getAllUsers(pageable);
    }

    @PutMapping("/api/v1/users/{id}")
    public void updateUser(@PathVariable UUID id, @RequestBody AdminUserAccessDTO adminUserAccessDTO) {
    }
}


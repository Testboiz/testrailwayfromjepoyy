package com.indivaragroup.jdt17wms.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardController {

    @GetMapping("/api/v1/admin-dashboard")
    public void getAdminDashboard() {
    }

    @GetMapping("/api/v1/me/dashboard")
    public void getUserDashboard() {
    }
}

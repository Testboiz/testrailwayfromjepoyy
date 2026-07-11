package com.indivaragroup.jdt17wms.controllers;

import com.indivaragroup.jdt17wms.dto.response.AdminDashboardDTO;
import com.indivaragroup.jdt17wms.dto.response.UserDashboardDTO;
import com.indivaragroup.jdt17wms.services.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/api/v1/admin-dashboard")
    public AdminDashboardDTO getAdminDashboard() {
      return dashboardService.getAdminDashboard();
    }

    @GetMapping("/api/v1/me/dashboard")
    public UserDashboardDTO getUserDashboard() {
        return dashboardService.getUserDashboard();
    }
}

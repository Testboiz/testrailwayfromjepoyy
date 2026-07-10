package com.indivaragroup.jdt17wms.controllers;

import com.indivaragroup.jdt17wms.services.AuditTrailManagementService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit")
public class AdminController {

    private final AuditTrailManagementService auditTrailManagementService;

    public AdminController(AuditTrailManagementService auditTrailManagementService) {
        this.auditTrailManagementService = auditTrailManagementService;
    }

    @GetMapping
    public void getAuditLogs() {
    }
}

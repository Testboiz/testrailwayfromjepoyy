package com.indivaragroup.jdt17wms.controllers;

import com.indivaragroup.jdt17wms.dto.response.ApiPath;
import com.indivaragroup.jdt17wms.dto.response.ApiResponse;
import com.indivaragroup.jdt17wms.dto.utils.ApiSuccess;
import com.indivaragroup.jdt17wms.models.AuditLog;
import com.indivaragroup.jdt17wms.services.AuditTrailManagementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPath.BASE_ADMIN_PATH)
public class AdminController {

    private final AuditTrailManagementService auditTrailManagementService;

    public AdminController(AuditTrailManagementService auditTrailManagementService) {
        this.auditTrailManagementService = auditTrailManagementService;
    }

    @GetMapping("/audit")
    public ApiResponse<Page<AuditLog>> getAuditLogs(
            @RequestParam(required = false, defaultValue = "false") Boolean headView,
            Pageable pageable) {
        return ApiResponse.success(ApiSuccess.AUDIT_LOGS_FETCHED,
                auditTrailManagementService.getAuditLogs(headView, pageable));
    }
}

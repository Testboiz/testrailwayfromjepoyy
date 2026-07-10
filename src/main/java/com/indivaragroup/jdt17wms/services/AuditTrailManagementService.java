package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.repositories.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditTrailManagementService {

    private final AuditLogRepository auditLogRepository;

    public AuditTrailManagementService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
}

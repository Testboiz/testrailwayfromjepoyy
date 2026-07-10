package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.models.AuditLog;
import com.indivaragroup.jdt17wms.repositories.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class AuditTrailManagementService {

    private final AuditLogRepository auditLogRepository;

    public AuditTrailManagementService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public Page<AuditLog> getAuditLogs(Boolean headView, Pageable pageable) {
        if (Boolean.TRUE.equals(headView)) {
            return auditLogRepository.findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "timestamp")));
        }
        return auditLogRepository.findAll(pageable);
    }
}

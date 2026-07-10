package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.models.AuditLog;
import com.indivaragroup.jdt17wms.repositories.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditTrailManagementServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditTrailManagementService auditTrailManagementService;

    @Test
    void serviceShouldBeInitialized() {
        assertNotNull(auditTrailManagementService);
    }

    @Test
    void getAuditLogs_whenHeadViewIsTrue_shouldQueryFirstFiveOrderedByTimestampDesc() {
        Page<AuditLog> mockPage = new PageImpl<>(Collections.emptyList());
        when(auditLogRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        Page<AuditLog> result = auditTrailManagementService.getAuditLogs(true, Pageable.unpaged());

        assertNotNull(result);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(auditLogRepository).findAll(pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        assertEquals(0, capturedPageable.getPageNumber());
        assertEquals(5, capturedPageable.getPageSize());
        Sort.Order order = capturedPageable.getSort().getOrderFor("timestamp");
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    @Test
    void getAuditLogs_whenHeadViewIsFalse_shouldQueryWithPassedPageable() {
        Page<AuditLog> mockPage = new PageImpl<>(Collections.emptyList());
        when(auditLogRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        Pageable passedPageable = PageRequest.of(2, 10, Sort.by("action"));
        Page<AuditLog> result = auditTrailManagementService.getAuditLogs(false, passedPageable);

        assertNotNull(result);
        verify(auditLogRepository).findAll(passedPageable);
    }
}

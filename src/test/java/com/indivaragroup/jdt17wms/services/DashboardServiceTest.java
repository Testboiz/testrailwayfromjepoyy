package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.repositories.UserRepository;
import com.indivaragroup.jdt17wms.repositories.AssetRepository;
import com.indivaragroup.jdt17wms.repositories.ProductRepository;
import com.indivaragroup.jdt17wms.repositories.AuditLogRepository;
import com.indivaragroup.jdt17wms.repositories.FinancialProfileRepository;
import com.indivaragroup.jdt17wms.repositories.TransactionHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private FinancialProfileRepository financialProfileRepository;

    @Mock
    private TransactionHistoryRepository transactionHistoryRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void serviceShouldBeInitialized() {
        assertNotNull(dashboardService);
    }
}

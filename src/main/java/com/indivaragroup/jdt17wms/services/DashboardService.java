package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.repositories.UserRepository;
import com.indivaragroup.jdt17wms.repositories.AssetRepository;
import com.indivaragroup.jdt17wms.repositories.ProductRepository;
import com.indivaragroup.jdt17wms.repositories.AuditLogRepository;
import com.indivaragroup.jdt17wms.repositories.FinancialProfileRepository;
import com.indivaragroup.jdt17wms.repositories.TransactionHistoryRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final ProductRepository productRepository;
    private final AuditLogRepository auditLogRepository;
    private final FinancialProfileRepository financialProfileRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;

    public DashboardService(UserRepository userRepository,
                            AssetRepository assetRepository,
                            ProductRepository productRepository,
                            AuditLogRepository auditLogRepository,
                            FinancialProfileRepository financialProfileRepository,
                            TransactionHistoryRepository transactionHistoryRepository) {
        this.userRepository = userRepository;
        this.assetRepository = assetRepository;
        this.productRepository = productRepository;
        this.auditLogRepository = auditLogRepository;
        this.financialProfileRepository = financialProfileRepository;
        this.transactionHistoryRepository = transactionHistoryRepository;
    }
}

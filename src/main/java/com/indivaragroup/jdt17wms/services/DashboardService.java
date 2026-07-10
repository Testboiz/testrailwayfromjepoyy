package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.dto.response.AdminDashboardDTO;
import com.indivaragroup.jdt17wms.dto.response.AumTrendDTO;
import com.indivaragroup.jdt17wms.dto.response.RiskProfilesDTO;
import com.indivaragroup.jdt17wms.models.Asset;
import com.indivaragroup.jdt17wms.repositories.UserRepository;
import com.indivaragroup.jdt17wms.repositories.AssetRepository;
import com.indivaragroup.jdt17wms.repositories.ProductRepository;
import com.indivaragroup.jdt17wms.repositories.AuditLogRepository;
import com.indivaragroup.jdt17wms.repositories.FinancialProfileRepository;
import com.indivaragroup.jdt17wms.repositories.TransactionHistoryRepository;
import com.indivaragroup.jdt17wms.repositories.ProductPriceRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final ProductRepository productRepository;
    private final AuditLogRepository auditLogRepository;
    private final FinancialProfileRepository financialProfileRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final ProductPriceRepository productPriceRepository;

    public DashboardService(UserRepository userRepository,
                            AssetRepository assetRepository,
                            ProductRepository productRepository,
                            AuditLogRepository auditLogRepository,
                            FinancialProfileRepository financialProfileRepository,
                            TransactionHistoryRepository transactionHistoryRepository,
                            ProductPriceRepository productPriceRepository) {
        this.userRepository = userRepository;
        this.assetRepository = assetRepository;
        this.productRepository = productRepository;
        this.auditLogRepository = auditLogRepository;
        this.financialProfileRepository = financialProfileRepository;
        this.transactionHistoryRepository = transactionHistoryRepository;
        this.productPriceRepository = productPriceRepository;
    }

    public AdminDashboardDTO getAdminDashboard() {
        AdminDashboardDTO adminDashboardDTO = new AdminDashboardDTO();
        RiskProfilesDTO riskProfilesDTO = new RiskProfilesDTO();
        riskProfilesDTO.setRiskAverse(userRepository.findByRiskProfile("risk_averse").size());
        riskProfilesDTO.setModerate(userRepository.findByRiskProfile("moderate").size());
        riskProfilesDTO.setRiskTaker(userRepository.findByRiskProfile("risk_taker").size());
        adminDashboardDTO.setAum(assetRepository.sumTotalAmount());
        adminDashboardDTO.setUserCount(userRepository.count());
        adminDashboardDTO.setProductCount(productRepository.count());
        adminDashboardDTO.setTotalAuditEvents(auditLogRepository.count());
        adminDashboardDTO.setRiskProfiles(riskProfilesDTO);
        adminDashboardDTO.setAumTrend(createAumTrend());

        return adminDashboardDTO;
    }

    public List<AumTrendDTO> createAumTrend() {
        Instant start = Instant.parse("2026-01-01T00:00:00Z");
        List<Asset> assets = assetRepository.findAllByPurchaseDateGreaterThanEqual(start);

        List<AumTrendDTO> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();
        int maxMonth = (today.getYear() == 2026) ? today.getMonthValue() : 12;

      for (int m = 1; m <= maxMonth; m++) {
        // 1. Set the snapshot to the LAST day of the month (e.g., 2026-07-31)
        LocalDate snapshotDate = LocalDate.of(2026, m, 1).with(TemporalAdjusters.lastDayOfMonth());

        // 2. Convert to the very end of that day (23:59:59.999) so it includes everything bought that day
        Instant snapshotInstant = snapshotDate.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC);

        BigDecimal monthlyAum = BigDecimal.ZERO;

        for (Asset asset : assets) {
          // July 10th is NOT after July 31st. This will now correctly evaluate to FALSE for Month 7!
          if (asset.getPurchaseDate().isAfter(snapshotInstant)) {
            continue;
          }

          // Pass the snapshotDate (which is now the end of the month) to get the latest price of that month
          BigDecimal price = productPriceRepository
            .findFirstByProductIdAndRecordedDateLessThanEqualOrderByRecordedDateDesc(asset.getProductId(), snapshotDate)
            .map(com.indivaragroup.jdt17wms.models.ProductPrice::getPrice)
            .orElse(BigDecimal.ZERO);

          BigDecimal assetValue = asset.getUnits().multiply(price);
          monthlyAum = monthlyAum.add(assetValue);
        }

        trend.add(AumTrendDTO.builder()
          .month(m)
          .value(monthlyAum)
          .build());
      }

        return trend;
    }
}

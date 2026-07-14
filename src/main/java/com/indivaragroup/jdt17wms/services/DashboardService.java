package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.dto.utils.SecurityUtils;
import com.indivaragroup.jdt17wms.dto.response.*;
import com.indivaragroup.jdt17wms.exceptions.MissingRiskProfileException;
import com.indivaragroup.jdt17wms.exceptions.NotFoundException;
import com.indivaragroup.jdt17wms.models.Asset;
import com.indivaragroup.jdt17wms.models.Product;
import com.indivaragroup.jdt17wms.models.User;
import com.indivaragroup.jdt17wms.repositories.UserRepository;
import com.indivaragroup.jdt17wms.repositories.AssetRepository;
import com.indivaragroup.jdt17wms.repositories.ProductRepository;
import com.indivaragroup.jdt17wms.repositories.AuditLogRepository;
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
  private final ProductPriceRepository productPriceRepository;

    public DashboardService(UserRepository userRepository,
                            AssetRepository assetRepository,
                            ProductRepository productRepository,
                            AuditLogRepository auditLogRepository,
                            ProductPriceRepository productPriceRepository) {
        this.userRepository = userRepository;
        this.assetRepository = assetRepository;
        this.productRepository = productRepository;
        this.auditLogRepository = auditLogRepository;
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
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
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

    public UserDashboardDTO getUserDashboard() {
        User user = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getQuestionnaireCompleted() == null || !user.getQuestionnaireCompleted()) {
            throw new MissingRiskProfileException("Risk Profiler Assessment Required");
        }

        List<Asset> assetList = assetRepository.findAllByUserId(user.getId());
        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalInvested = BigDecimal.ZERO;
        List<PortfolioItemDTO> portfolioItemDTOList = new ArrayList<>();

        for (Asset asset : assetList) {
            Product product = productRepository.findById(asset.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found"));

            BigDecimal assetValue = asset.getUnits().multiply(product.getCurrentPrice());
            totalValue = totalValue.add(assetValue);
            totalInvested = totalInvested.add(asset.getAmount());

            PortfolioItemDTO portfolioItemDTO = PortfolioItemDTO.builder()
                    .name(product.getName())
                    .value(assetValue)
                    .build();
            portfolioItemDTOList.add(portfolioItemDTO);
        }

        PortfolioDTO portfolioDTO = PortfolioDTO.builder()
                .value(totalValue.toString())
                .invested(totalInvested.toString())
                .holdings(portfolioItemDTOList.size())
                .items(portfolioItemDTOList)
                .build();

        List<PerformanceDTO> performance = createUserPerformanceTrend(user.getId());

        return UserDashboardDTO.builder()
                .portofolio(portfolioDTO)
                .performance(performance)
                .build();
    }

    private List<PerformanceDTO> createUserPerformanceTrend(java.util.UUID userId) {
        List<Asset> assets = assetRepository.findAllByUserId(userId);

        List<PerformanceDTO> trend = new ArrayList<>();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        int maxMonth = (today.getYear() == 2026) ? today.getMonthValue() : 12;

        for (int m = 1; m <= maxMonth; m++) {
            LocalDate snapshotDate = LocalDate.of(2026, m, 1).with(TemporalAdjusters.lastDayOfMonth());
            Instant snapshotInstant = snapshotDate.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC);
            BigDecimal monthlyValue = BigDecimal.ZERO;

            for (Asset asset : assets) {
                if (asset.getPurchaseDate().isAfter(snapshotInstant)) {
                    continue;
                }

                BigDecimal price = productPriceRepository
                        .findFirstByProductIdAndRecordedDateLessThanEqualOrderByRecordedDateDesc(asset.getProductId(), snapshotDate)
                        .map(com.indivaragroup.jdt17wms.models.ProductPrice::getPrice)
                        .orElse(BigDecimal.ZERO);

                BigDecimal assetValue = asset.getUnits().multiply(price);
                monthlyValue = monthlyValue.add(assetValue);
            }

            trend.add(PerformanceDTO.builder()
                    .month(m)
                    .value(monthlyValue)
                    .build());
        }

        return trend;
    }
}

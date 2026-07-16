package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.dto.utils.ApiError;
import com.indivaragroup.jdt17wms.dto.utils.SecurityUtils;
import com.indivaragroup.jdt17wms.dto.response.*;
import com.indivaragroup.jdt17wms.exceptions.CoreThrowHandler;
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
import java.time.Month;
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
        Map<String, Long> riskMap = userRepository.countByRiskProfile().stream()
                .collect(Collectors.toMap(
                        UserRepository.RiskProfileCount::getRiskProfile,
                        UserRepository.RiskProfileCount::getCount));

        RiskProfilesDTO riskProfiles = RiskProfilesDTO.builder()
                .riskAverse(riskMap.getOrDefault("risk_averse", 0L).intValue())
                .moderate(riskMap.getOrDefault("moderate", 0L).intValue())
                .riskTaker(riskMap.getOrDefault("risk_taker", 0L).intValue())
                .build();

        return AdminDashboardDTO.builder()
                .aum(assetRepository.sumTotalAmount())
                .userCount(userRepository.count())
                .productCount(productRepository.count())
                .totalAuditEvents(auditLogRepository.count())
                .riskProfiles(riskProfiles)
                .aumTrend(createAumTrend())
                .build();
    }

  public List<AumTrendDTO> createAumTrend() {
    LocalDate today = LocalDate.now(ZoneOffset.UTC);
    int maxMonth = today.getMonthValue(); // e.g., July = 7
    int currentYear = today.getYear();

    // Dynamically query assets purchased since Jan 1st of the current year
    Instant startOfYear = LocalDate.of(currentYear, Month.JANUARY, 1).atStartOfDay(ZoneOffset.UTC).toInstant();
    List<Asset> assets = assetRepository.findAllByPurchaseDateGreaterThanEqual(startOfYear);

    List<AumTrendDTO> trend = new ArrayList<>();

    for (int m = 1; m <= maxMonth; m++) {
      LocalDate snapshotDate = LocalDate.of(currentYear, m, 1).with(TemporalAdjusters.lastDayOfMonth());
      Instant snapshotInstant = snapshotDate.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC);

      BigDecimal monthlyAum = BigDecimal.ZERO;

      for (Asset asset : assets) {
        if (asset.getPurchaseDate().isAfter(snapshotInstant)) {
          continue;
        }

        BigDecimal price = productPriceRepository
          .findFirstByProductIdAndRecordedDateLessThanEqualOrderByRecordedDateDesc(asset.getProductId(), snapshotDate)
          .map(com.indivaragroup.jdt17wms.models.ProductPrice::getPrice)
          .orElse(BigDecimal.ZERO);

        monthlyAum = monthlyAum.add(asset.getUnits().multiply(price));
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
      .orElseThrow(() -> new CoreThrowHandler(ApiError.USER_NOT_FOUND));
    if (user.getQuestionnaireCompleted() == null || !user.getQuestionnaireCompleted()) {
      throw new CoreThrowHandler(ApiError.REQUIRED_RISK_PROFILER);
    }

    List<Asset> assetList = assetRepository.findAllByUserId(user.getId());
    BigDecimal totalValue = BigDecimal.ZERO;
    BigDecimal totalInvested = BigDecimal.ZERO;
    List<PortfolioItemDTO> portfolioItemDTOList = new ArrayList<>();

    for (Asset asset : assetList) {
      Product product = productRepository.findById(asset.getProductId())
        .orElseThrow(() -> new CoreThrowHandler(ApiError.ITEM_NOT_FOUND));

      BigDecimal units = Optional.ofNullable(asset.getUnits())
        .orElseThrow(() -> new CoreThrowHandler(ApiError.BAD_REQUEST, "Asset " + asset.getId() + " has null units — data corrupt"));
      BigDecimal currentPrice = Optional.ofNullable(product.getCurrentPrice())
        .orElseThrow(() -> new CoreThrowHandler(ApiError.BAD_REQUEST, "Product " + product.getId() + " has null currentPrice — data corrupt"));
      BigDecimal assetValue = units.multiply(currentPrice);
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
    int maxMonth = today.getMonthValue();
    int currentYear = today.getYear();

    for (int m = 1; m <= maxMonth; m++) {
      LocalDate snapshotDate = LocalDate.of(currentYear, m, 1).with(TemporalAdjusters.lastDayOfMonth());
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

        monthlyValue = monthlyValue.add(asset.getUnits().multiply(price));
      }

      trend.add(PerformanceDTO.builder()
        .month(m)
        .value(monthlyValue)
        .build());
    }

    return trend;
  }
}

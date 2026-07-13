package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.constants.AppConstants;
import com.indivaragroup.jdt17wms.dto.response.UserDashboardDTO;
import com.indivaragroup.jdt17wms.exceptions.MissingRiskProfileException;
import com.indivaragroup.jdt17wms.exceptions.NotFoundException;
import com.indivaragroup.jdt17wms.models.Asset;
import com.indivaragroup.jdt17wms.models.Product;
import com.indivaragroup.jdt17wms.models.ProductPrice;
import com.indivaragroup.jdt17wms.models.User;
import com.indivaragroup.jdt17wms.repositories.UserRepository;
import com.indivaragroup.jdt17wms.repositories.AssetRepository;
import com.indivaragroup.jdt17wms.repositories.ProductRepository;
import com.indivaragroup.jdt17wms.repositories.AuditLogRepository;
import com.indivaragroup.jdt17wms.repositories.FinancialProfileRepository;
import com.indivaragroup.jdt17wms.repositories.TransactionHistoryRepository;
import com.indivaragroup.jdt17wms.repositories.ProductPriceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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

    @Mock
    private ProductPriceRepository productPriceRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void serviceShouldBeInitialized() {
        assertNotNull(dashboardService);
    }

    @Test
    void getUserDashboard_shouldReturnDashboardWhenQuestionnaireCompletedIsTrue() {
        User user = User.builder()
                .id(AppConstants.USER_ID)
                .questionnaireCompleted(true)
                .build();

        UUID productId = UUID.randomUUID();
        Asset asset = Asset.builder()
                .userId(AppConstants.USER_ID)
                .productId(productId)
                .units(new BigDecimal("10.00"))
                .amount(new BigDecimal("100.00"))
                .purchaseDate(Instant.parse("2026-01-10T10:00:00Z"))
                .build();

        Product product = Product.builder()
                .id(productId)
                .name("Test Product")
                .currentPrice(new BigDecimal("12.00"))
                .build();

        ProductPrice productPrice = ProductPrice.builder()
                .productId(productId)
                .price(new BigDecimal("11.50"))
                .build();

        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.of(user));
        when(assetRepository.findAllByUserId(AppConstants.USER_ID)).thenReturn(List.of(asset));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productPriceRepository.findFirstByProductIdAndRecordedDateLessThanEqualOrderByRecordedDateDesc(
                eq(productId), any(LocalDate.class)))
                .thenReturn(Optional.of(productPrice));

        UserDashboardDTO result = dashboardService.getUserDashboard();

        assertNotNull(result);
        assertNotNull(result.getPortofolio());
        assertEquals("120.0000", result.getPortofolio().getValue()); // 10 units * 12.00 price = 120.0000
        assertEquals("100.00", result.getPortofolio().getInvested());
        assertEquals(1, result.getPortofolio().getHoldings());
        assertEquals("Test Product", result.getPortofolio().getItems().getFirst().getName());
        assertNotNull(result.getPerformance());
    }

    @Test
    void getUserDashboard_shouldThrowMissingRiskProfileExceptionWhenQuestionnaireCompletedIsFalse() {
        User user = User.builder()
                .id(AppConstants.USER_ID)
                .questionnaireCompleted(false)
                .build();

        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.of(user));

        assertThrows(MissingRiskProfileException.class, () -> dashboardService.getUserDashboard());
    }

    @Test
    void getUserDashboard_shouldThrowMissingRiskProfileExceptionWhenQuestionnaireCompletedIsNull() {
        User user = User.builder()
                .id(AppConstants.USER_ID)
                .questionnaireCompleted(null)
                .build();

        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.of(user));

        assertThrows(MissingRiskProfileException.class, () -> dashboardService.getUserDashboard());
    }

    @Test
    void getUserDashboard_shouldThrowNotFoundExceptionWhenUserNotFound() {
        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> dashboardService.getUserDashboard());
    }
}

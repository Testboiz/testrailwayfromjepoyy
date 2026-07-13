package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.constants.AppConstants;
import com.indivaragroup.jdt17wms.dto.response.GoalProjectionDTO;
import com.indivaragroup.jdt17wms.exceptions.MissingRiskProfileException;
import com.indivaragroup.jdt17wms.exceptions.NotFoundException;
import com.indivaragroup.jdt17wms.models.FinancialProfile;
import com.indivaragroup.jdt17wms.models.Goal;
import com.indivaragroup.jdt17wms.models.User;
import com.indivaragroup.jdt17wms.models.Asset;
import com.indivaragroup.jdt17wms.models.Product;
import com.indivaragroup.jdt17wms.repositories.FinancialProfileRepository;
import com.indivaragroup.jdt17wms.repositories.GoalRepository;
import com.indivaragroup.jdt17wms.repositories.UserRepository;
import com.indivaragroup.jdt17wms.repositories.AssetRepository;
import com.indivaragroup.jdt17wms.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoalsProjectionServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FinancialProfileRepository financialProfileRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private GoalsProjectionService goalsProjectionService;

    @Test
    void serviceShouldBeInitialized() {
        assertNotNull(goalsProjectionService);
    }

    @Test
    void getProjectionsForUser_shouldReturnProjectionsWhenNoAssets() {
        User user = User.builder()
                .id(AppConstants.USER_ID)
                .questionnaireCompleted(true)
                .build();
        Goal goal = Goal.builder()
                .id(UUID.randomUUID())
                .userId(AppConstants.USER_ID)
                .name("Retirement Fund")
                .targetAmount(new BigDecimal("500000.00"))
                .monthlyContribution(new BigDecimal("1000.00"))
                .targetDate(LocalDate.now().plusYears(10))
                .currentAmount(BigDecimal.ZERO)
                .build();
        FinancialProfile profile = FinancialProfile.builder()
                .defaultReturn(new BigDecimal("7.50"))
                .build();

        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.of(user));
        when(financialProfileRepository.findByUserId(AppConstants.USER_ID)).thenReturn(Optional.of(profile));
        when(goalRepository.findAllByUserId(AppConstants.USER_ID)).thenReturn(List.of(goal));
        when(assetRepository.findAllByGoalId(any(UUID.class))).thenReturn(List.of());

        List<GoalProjectionDTO> results = goalsProjectionService.getProjectionsForUser();

        assertNotNull(results);
        assertEquals(1, results.size());
        GoalProjectionDTO res = results.get(0);
        assertEquals("Retirement Fund", res.getName());

        // Under 0% savings growth, projectedDate should be plus 500 months (500000 target / 1000 contribution)
        LocalDate expectedProjectedDate = LocalDate.now(java.time.ZoneOffset.UTC).plusMonths(500);
        assertEquals(expectedProjectedDate, res.getProjectedDate());

        // Under 0% savings growth, recommendedContribution should be target / months = 500000 / 120 = 4166.67
        assertEquals(new BigDecimal("4166.67"), res.getRecommendedContribution());

        assertNotNull(res.getTimeSeries());
        assertEquals(60, res.getTimeSeries().size());
        assertEquals(new BigDecimal("1000.00"), res.getTimeSeries().get(0).getValue());
        assertEquals(new BigDecimal("60000.00"), res.getTimeSeries().get(59).getValue());
    }

    @Test
    void getProjectionsForUser_shouldReturnProjectionsWhenAssetsTied() {
        User user = User.builder()
                .id(AppConstants.USER_ID)
                .questionnaireCompleted(true)
                .build();
        UUID goalId = UUID.randomUUID();
        Goal goal = Goal.builder()
                .id(goalId)
                .userId(AppConstants.USER_ID)
                .name("Retirement Fund")
                .targetAmount(new BigDecimal("500000.00"))
                .monthlyContribution(new BigDecimal("1000.00"))
                .targetDate(LocalDate.now().plusYears(10))
                .currentAmount(BigDecimal.ZERO)
                .build();
        FinancialProfile profile = FinancialProfile.builder()
                .defaultReturn(new BigDecimal("7.50"))
                .build();
        UUID productId = UUID.randomUUID();
        Asset asset = Asset.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .goalId(goalId)
                .currentValue(new BigDecimal("10000.00"))
                .build();
        Product product = Product.builder()
                .id(productId)
                .annualReturn(new BigDecimal("12.00"))
                .build();

        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.of(user));
        when(financialProfileRepository.findByUserId(AppConstants.USER_ID)).thenReturn(Optional.of(profile));
        when(goalRepository.findAllByUserId(AppConstants.USER_ID)).thenReturn(List.of(goal));
        when(assetRepository.findAllByGoalId(goalId)).thenReturn(List.of(asset));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        List<GoalProjectionDTO> results = goalsProjectionService.getProjectionsForUser();

        assertNotNull(results);
        assertEquals(1, results.size());
        GoalProjectionDTO res = results.get(0);
        assertEquals("Retirement Fund", res.getName());
        assertNotNull(res.getProjectedDate());
        assertNotNull(res.getRecommendedContribution());
        assertNotNull(res.getTimeSeries());
        assertEquals(60, res.getTimeSeries().size());
    }

    @Test
    void getProjectionsForUser_shouldThrowMissingRiskProfileExceptionWhenQuestionnaireNotCompleted() {
        User user = User.builder()
                .id(AppConstants.USER_ID)
                .questionnaireCompleted(false)
                .build();

        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.of(user));

        assertThrows(MissingRiskProfileException.class, () -> goalsProjectionService.getProjectionsForUser());
    }

    @Test
    void getProjectionsForUser_shouldThrowNotFoundExceptionWhenUserNotFound() {
        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> goalsProjectionService.getProjectionsForUser());
    }
}

package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.dto.response.ComponentDTO;
import com.indivaragroup.jdt17wms.dto.response.HealthDTO;
import com.indivaragroup.jdt17wms.dto.response.RecommendationDTO;
import com.indivaragroup.jdt17wms.exceptions.NotFoundException;
import com.indivaragroup.jdt17wms.models.*;
import com.indivaragroup.jdt17wms.models.enums.RecommendationStatus;
import com.indivaragroup.jdt17wms.repositories.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.indivaragroup.jdt17wms.dto.response.UserDTO;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActionRecommendationServiceTest {

    @Mock
    private RecommendationRepository recommendationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AssetRepository assetRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private GoalRepository goalRepository;
    @Mock
    private FinancialProfileRepository financialProfileRepository;
    @Mock
    private ExpenseRepository expenseRepository;

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-14T12:00:00Z"), ZoneOffset.UTC);

    private ActionRecommendationService actionRecommendationService;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        actionRecommendationService = new ActionRecommendationService(
                recommendationRepository,
                userRepository,
                assetRepository,
                productRepository,
                goalRepository,
                financialProfileRepository,
                expenseRepository,
                clock
        );
        UserDTO userDTO = UserDTO.builder()
                .id(userId)
                .email("test@example.com")
                .isAdmin(false)
                .build();
        Authentication auth = new UsernamePasswordAuthenticationToken(userDTO, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ==========================================
    //  getHealthScore Tests
    // ==========================================

    @Test
    void getHealthScore_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> actionRecommendationService.getHealthScore());
    }

    @Test
    void getHealthScore_HappyPath_ExcellentScore() {
        User user = User.builder().id(userId).riskProfile("moderate").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Create products
        Product pDeposit = Product.builder().id(UUID.randomUUID()).type("deposit").riskLevel(1).visible(true).build();
        Product pStock = Product.builder().id(UUID.randomUUID()).type("stock").riskLevel(4).visible(true).build();
        Product pBond = Product.builder().id(UUID.randomUUID()).type("bond").riskLevel(2).visible(true).build();
        when(productRepository.findAll()).thenReturn(List.of(pDeposit, pStock, pBond));

        // Create assets
        Asset a1 = Asset.builder().productId(pDeposit.getId()).currentValue(BigDecimal.valueOf(500000)).build();
        Asset a2 = Asset.builder().productId(pStock.getId()).currentValue(BigDecimal.valueOf(500000)).build();
        when(assetRepository.findAllByUserId(userId)).thenReturn(List.of(a1, a2));

        // Create goals
        Goal g1 = Goal.builder().type("retirement").build(); // retirement matches stock, bond, deposit
        Goal g2 = Goal.builder().type("vacation").build();   // vacation matches deposit
        when(goalRepository.findAllByUserId(userId)).thenReturn(List.of(g1, g2));

        // Create financial profile
        FinancialProfile fp = FinancialProfile.builder().id(UUID.randomUUID()).monthlyIncome(BigDecimal.valueOf(20000)).build();
        when(financialProfileRepository.findByUserId(userId)).thenReturn(Optional.of(fp));

        // Create expense
        Expense exp = Expense.builder().totalExpenses(BigDecimal.valueOf(10000)).build();
        when(expenseRepository.findByFinancialProfileId(fp.getId())).thenReturn(Optional.of(exp));

        HealthDTO health = actionRecommendationService.getHealthScore();

        assertNotNull(health);
        assertEquals("Excellent", health.getStatus());
        assertTrue(health.getTotalScore() >= 80);
        assertEquals(BigDecimal.valueOf(1000000), health.getPortofolioValue());
        assertEquals(BigDecimal.valueOf(10000), health.getAvailableSurplus());

        // Check components are calculated
        assertNotNull(health.getComponents());
        assertEquals(4, health.getComponents().size());
    }

    @Test
    void getHealthScore_NoExpenses_ReturnsMidpointEmergencyScore() {
        User user = User.builder().id(userId).riskProfile("moderate").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findAll()).thenReturn(Collections.emptyList());
        when(assetRepository.findAllByUserId(userId)).thenReturn(Collections.emptyList());
        when(goalRepository.findAllByUserId(userId)).thenReturn(Collections.emptyList());

        // Monthly income is set, expenses returns 0 (missing expense)
        FinancialProfile fp = FinancialProfile.builder().id(UUID.randomUUID()).monthlyIncome(BigDecimal.valueOf(20000)).build();
        when(financialProfileRepository.findByUserId(userId)).thenReturn(Optional.of(fp));
        when(expenseRepository.findByFinancialProfileId(fp.getId())).thenReturn(Optional.empty());

        HealthDTO health = actionRecommendationService.getHealthScore();

        assertNotNull(health);
        // Find emergency fund component
        ComponentDTO emergencyComp = health.getComponents().stream()
                .filter(c -> "emergency".equals(c.getComponentName()))
                .findFirst().orElseThrow();
        assertEquals(12, emergencyComp.getScore());
    }

    @Test
    void getHealthScore_NoEligibleProducts_ReturnsZeroDiversification() {
        User user = User.builder().id(userId).riskProfile("moderate").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Products are either invisible or risk level is too high for "moderate" (max risk 4)
        Product pInvisible = Product.builder().id(UUID.randomUUID()).type("deposit").riskLevel(1).visible(false).build();
        Product pHighRisk = Product.builder().id(UUID.randomUUID()).type("stock").riskLevel(5).visible(true).build();
        when(productRepository.findAll()).thenReturn(List.of(pInvisible, pHighRisk));

        when(assetRepository.findAllByUserId(userId)).thenReturn(Collections.emptyList());
        when(goalRepository.findAllByUserId(userId)).thenReturn(Collections.emptyList());
        when(financialProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        HealthDTO health = actionRecommendationService.getHealthScore();

        assertNotNull(health);
        ComponentDTO divComp = health.getComponents().stream()
                .filter(c -> "diversification".equals(c.getComponentName()))
                .findFirst().orElseThrow();
        assertEquals(0, divComp.getScore());
    }

    @Test
    void getHealthScore_NoGoals_ReturnsMidpointGoalCoverage() {
        User user = User.builder().id(userId).riskProfile("moderate").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findAll()).thenReturn(Collections.emptyList());
        when(assetRepository.findAllByUserId(userId)).thenReturn(Collections.emptyList());
        when(goalRepository.findAllByUserId(userId)).thenReturn(Collections.emptyList()); // empty goals
        when(financialProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        HealthDTO health = actionRecommendationService.getHealthScore();

        assertNotNull(health);
        ComponentDTO goalComp = health.getComponents().stream()
                .filter(c -> "goalCoverage".equals(c.getComponentName()))
                .findFirst().orElseThrow();
        assertEquals(12, goalComp.getScore());
    }

    @Test
    void getHealthScore_ZeroPortfolioValue_ReturnsMidpointRiskAlignment() {
        User user = User.builder().id(userId).riskProfile("moderate").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findAll()).thenReturn(Collections.emptyList());
        when(assetRepository.findAllByUserId(userId)).thenReturn(Collections.emptyList()); // zero assets
        when(goalRepository.findAllByUserId(userId)).thenReturn(Collections.emptyList());
        when(financialProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        HealthDTO health = actionRecommendationService.getHealthScore();

        assertNotNull(health);
        ComponentDTO riskComp = health.getComponents().stream()
                .filter(c -> "riskAlignment".equals(c.getComponentName()))
                .findFirst().orElseThrow();
        assertEquals(12, riskComp.getScore());
    }

    @ParameterizedTest
    @CsvSource({
        "moderate, 1, 4, 25",
        "moderate, 2, 5, 20",
        "moderate, 3, 5, 14",
        "moderate, 4, 5, 8",
        "moderate, 5, 5, 4",
        "risk_averse, 1, 2, 25",
        "risk_taker, 1, 5, 20",
        "null, 1, 4, 25"
    })
    void getHealthScore_RiskAlignment(String riskProfile, int riskLevel1, int riskLevel2, int expectedRiskAlignmentScore) {
        String profileToUse = "null".equals(riskProfile) ? null : riskProfile;
        User user = User.builder().id(userId).riskProfile(profileToUse).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Product pDeposit = Product.builder().id(UUID.randomUUID()).type("deposit").riskLevel(riskLevel1).visible(true).build();
        Product pStock = Product.builder().id(UUID.randomUUID()).type("stock").riskLevel(riskLevel2).visible(true).build();
        when(productRepository.findAll()).thenReturn(List.of(pDeposit, pStock));
        Asset a1 = Asset.builder().productId(pDeposit.getId()).currentValue(BigDecimal.valueOf(100000)).build();
        Asset a2 = Asset.builder().productId(pStock.getId()).currentValue(BigDecimal.valueOf(100000)).build();
        when(assetRepository.findAllByUserId(userId)).thenReturn(List.of(a1, a2));
        when(goalRepository.findAllByUserId(userId)).thenReturn(Collections.emptyList());
        when(financialProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        HealthDTO health = actionRecommendationService.getHealthScore();
        assertNotNull(health);

        ComponentDTO riskComp = health.getComponents().stream()
                .filter(c -> "riskAlignment".equals(c.getComponentName()))
                .findFirst().orElseThrow();
        assertEquals(expectedRiskAlignmentScore, riskComp.getScore());
    }

    // ==========================================
    //  generateRecommendations Tests
    // ==========================================

    @Test
    void generateRecommendations_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> actionRecommendationService.generateRecommendations());
    }

    @Test
    void generateRecommendations_AllRulesTriggered() {
        User user = User.builder().id(userId).riskProfile("risk_taker").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Create products
        Product pDeposit = Product.builder()
                .id(UUID.randomUUID())
                .name("Liquid Deposit")
                .type("deposit")
                .riskLevel(1)
                .visible(true)
                .annualReturn(BigDecimal.valueOf(4.5))
                .minInvestment(BigDecimal.valueOf(1000))
                .build();

        Product pStock = Product.builder()
                .id(UUID.randomUUID())
                .name("Growth Stock")
                .type("stock")
                .riskLevel(4)
                .visible(true)
                .annualReturn(BigDecimal.valueOf(12.0))
                .minInvestment(BigDecimal.valueOf(5000))
                .build();

        Product pBond = Product.builder()
                .id(UUID.randomUUID())
                .name("Government Bond")
                .type("bond")
                .riskLevel(2)
                .visible(true)
                .annualReturn(BigDecimal.valueOf(6.0))
                .minInvestment(BigDecimal.valueOf(2000))
                .build();

        when(productRepository.findAll()).thenReturn(List.of(pDeposit, pStock, pBond));

        // Create assets to trigger Rule 2 (Concentration Risk: >65% in one product)
        Asset aDeposit = Asset.builder()
                .productId(pDeposit.getId())
                .currentValue(BigDecimal.valueOf(35000))
                .build();
        when(assetRepository.findAllByUserId(userId)).thenReturn(List.of(aDeposit));

        // Create goals to trigger Rule 3 (Priority goal alignment) and Rule 4 (Other goal alignment)
        Goal priorityGoal = Goal.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .name("Property Goal")
                .type("property") // Property needs balanced_fund, bond, sukuk, stock (owned "deposit" doesn't match)
                .targetAmount(BigDecimal.valueOf(500000))
                .monthlyContribution(BigDecimal.valueOf(5000))
                .targetDate(LocalDate.now(clock).plusYears(10))
                .isPriority(true)
                .build();

        Goal otherGoal = Goal.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .name("Property Goal 2")
                .type("property")
                .targetAmount(BigDecimal.valueOf(10000))
                .monthlyContribution(BigDecimal.valueOf(1000))
                .isPriority(false)
                .build();

        when(goalRepository.findAllByUserId(userId)).thenReturn(List.of(priorityGoal, otherGoal));

        FinancialProfile fp = FinancialProfile.builder()
                .id(UUID.randomUUID())
                .monthlyIncome(BigDecimal.valueOf(250000)) // High income to trigger Rule 7 (Idle Surplus)
                .build();
        when(financialProfileRepository.findByUserId(userId)).thenReturn(Optional.of(fp));

        Expense exp = Expense.builder()
                .totalExpenses(BigDecimal.valueOf(10000)) // emergency target = 60,000 > 35,000 -> triggers Rule 1
                .build();
        when(expenseRepository.findByFinancialProfileId(fp.getId())).thenReturn(Optional.of(exp));

        when(recommendationRepository.findAllByUserIdAndStatus(userId, RecommendationStatus.PENDING))
                .thenReturn(Collections.emptyList());
        when(recommendationRepository.saveAllAndFlush(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<RecommendationDTO> recs = actionRecommendationService.generateRecommendations();

        assertNotNull(recs);
        assertFalse(recs.isEmpty());

        boolean hasEmergency = recs.stream().anyMatch(r -> "emergency".equals(r.getCategory()));
        boolean hasConcentration = recs.stream().anyMatch(r -> "rebalance".equals(r.getCategory()));
        boolean hasGoal = recs.stream().anyMatch(r -> "goal".equals(r.getCategory()));
        boolean hasDiversification = recs.stream().anyMatch(r -> "diversification".equals(r.getCategory()));
        boolean hasSurplus = recs.stream().anyMatch(r -> "surplus".equals(r.getCategory()));

        assertTrue(hasEmergency, "Rule 1: Emergency Shortfall should be triggered");
        assertTrue(hasConcentration, "Rule 2: Concentration Risk should be triggered");
        assertTrue(hasGoal, "Rule 3/4: Goal alignment should be triggered");
        assertTrue(hasDiversification, "Rule 5: Diversification Gaps should be triggered");
        assertTrue(hasSurplus, "Rule 7: Idle surplus should be triggered");
    }

    @Test
    void generateRecommendations_Reconciliation_ActiveAndInactivePending() {
        User user = User.builder().id(userId).riskProfile("moderate").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(productRepository.findAll()).thenReturn(Collections.emptyList());
        when(assetRepository.findAllByUserId(userId)).thenReturn(Collections.emptyList());
        when(goalRepository.findAllByUserId(userId)).thenReturn(Collections.emptyList());
        when(financialProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        UUID activeRecProductId = UUID.randomUUID();
        UUID inactiveRecProductId = UUID.randomUUID();

        Product pGrowth = Product.builder()
                .id(activeRecProductId)
                .name("Growth Product")
                .type("stock")
                .riskLevel(3)
                .visible(true)
                .annualReturn(BigDecimal.valueOf(10.0))
                .minInvestment(BigDecimal.valueOf(1000))
                .build();
        when(productRepository.findAll()).thenReturn(List.of(pGrowth));

        // Use category "diversification" since it is generated by Rule 5 (missing stock exposure)
        Recommendation pendingActive = Recommendation.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .category("diversification")
                .productId(activeRecProductId)
                .status(RecommendationStatus.PENDING)
                .title("Old Title")
                .reason("Old Reason")
                .build();

        Recommendation pendingInactive = Recommendation.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .category("diversification")
                .productId(inactiveRecProductId)
                .status(RecommendationStatus.PENDING)
                .title("Old Diversification Title")
                .build();

        when(recommendationRepository.findAllByUserIdAndStatus(userId, RecommendationStatus.PENDING))
                .thenReturn(new ArrayList<>(List.of(pendingActive, pendingInactive)));
        when(recommendationRepository.saveAllAndFlush(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<RecommendationDTO> recs = actionRecommendationService.generateRecommendations();

        assertNotNull(recs);
        assertEquals(1, recs.size());
        RecommendationDTO updated = recs.getFirst();
        assertEquals(pendingActive.getId(), updated.getId());
        assertNotEquals("Old Title", updated.getTitle());

        assertEquals(RecommendationStatus.APPLIED, pendingInactive.getStatus());
        assertNotNull(pendingInactive.getResolvedAt());
    }
}

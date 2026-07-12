package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.constants.AppConstants;
import com.indivaragroup.jdt17wms.dto.response.ComponentDTO;
import com.indivaragroup.jdt17wms.dto.response.HealthDTO;
import com.indivaragroup.jdt17wms.exceptions.NotFoundException;
import com.indivaragroup.jdt17wms.models.*;
import com.indivaragroup.jdt17wms.repositories.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ActionRecommendationService {

    // ── Liquid product types (used for emergency fund calculation) ──
    private static final Set<String> LIQUID_PRODUCT_TYPES = Set.of("money_market", "deposit");

    // ── Goal type → suitable product types mapping ──
    // Based on inherent risk:
    //   Low risk products:    money_market, deposit, balanced_fund
    //   Medium risk products: bond, sukuk (sharia equivalent of bond)
    //   High risk products:   stock
    private static final Map<String, List<String>> GOAL_PRODUCT_TYPES = Map.of(
            "emergency_fund", List.of("money_market", "deposit", "balanced_fund"),
            "vacation", List.of("money_market", "deposit", "balanced_fund"),
            "vehicle_purchase", List.of("money_market", "deposit", "balanced_fund", "bond", "sukuk"),
            "property", List.of("balanced_fund", "bond", "sukuk", "stock"),
            "retirement", List.of("stock", "bond", "sukuk", "balanced_fund", "money_market", "deposit"),
            "custom", List.of("stock", "money_market", "balanced_fund", "bond", "sukuk", "deposit")
    );

    // ── Risk profile → max allowed risk level ──
    // Mirrors the logic in ProductManagementService
    private static final Map<String, Integer> MAX_RISK_LEVELS = Map.of(
            "risk_averse", 2,
            "moderate", 4,
            "risk_taker", 5
    );

    // ── Risk profile → target weighted average portfolio risk ──
    private static final Map<String, Double> RISK_TARGETS = Map.of(
            "risk_averse", 1.5,
            "moderate", 2.5,
            "risk_taker", 4.0
    );

    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final ProductRepository productRepository;
    private final GoalRepository goalRepository;
    private final FinancialProfileRepository financialProfileRepository;
    private final ExpenseRepository expenseRepository;

    public ActionRecommendationService(RecommendationRepository recommendationRepository,
                                       UserRepository userRepository,
                                       AssetRepository assetRepository,
                                       ProductRepository productRepository,
                                       GoalRepository goalRepository,
                                       FinancialProfileRepository financialProfileRepository,
                                       ExpenseRepository expenseRepository) {
        this.recommendationRepository = recommendationRepository;
        this.userRepository = userRepository;
        this.assetRepository = assetRepository;
        this.productRepository = productRepository;
        this.goalRepository = goalRepository;
        this.financialProfileRepository = financialProfileRepository;
        this.expenseRepository = expenseRepository;
    }

    /**
     * Calculates the user's financial health score (0–100) composed of four
     * equally-weighted components (25 pts each):
     * 1. Emergency Fund — liquid assets vs. 6× monthly expenses
     * 2. Diversification — unique product types owned vs. eligible
     * 3. Goal Coverage — goals with a matching product type in portfolio
     * 4. Risk Alignment — weighted avg portfolio risk vs. profile target
     */
    public HealthDTO getHealthScore() {
        // ── Fetch all required data ──
        User user = userRepository.findById(AppConstants.USER_ID)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<Asset> assets = assetRepository.findAllByUserId(user.getId());
        List<Product> products = productRepository.findAll();
        List<Goal> goals = goalRepository.findAllByUserId(user.getId());

        BigDecimal monthlyExpenses = BigDecimal.ZERO;
        BigDecimal monthlyIncome = BigDecimal.ZERO;

        FinancialProfile finProfile = financialProfileRepository
                .findByUserId(user.getId()).orElse(null);

        if (finProfile != null) {
            monthlyIncome = finProfile.getMonthlyIncome() != null
                    ? finProfile.getMonthlyIncome() : BigDecimal.ZERO;

            Expense expense = expenseRepository
                    .findByFinancialProfileId(finProfile.getId()).orElse(null);
            if (expense != null && expense.getTotalExpenses() != null) {
                monthlyExpenses = expense.getTotalExpenses();
            }
        }

        // ── Portfolio total value ──
        BigDecimal totalValue = assets.stream()
                .map(a -> a.getCurrentValue() != null ? a.getCurrentValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ── Max risk level for user's profile ──
        String riskProfile = user.getRiskProfile();
        int maxRiskLv = 5;
        if (riskProfile != null) {
            maxRiskLv = MAX_RISK_LEVELS.getOrDefault(riskProfile.toLowerCase(), 5);
        }

        // Product lookup by ID for O(1) access
        Map<UUID, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // ══════════════════════════════════════════════════════════════
        // COMPONENT 1: Emergency Fund (25 pts)
        // Target = 6× monthly expenses in liquid assets (money_market, deposit)
        // ══════════════════════════════════════════════════════════════
        BigDecimal emergencyTarget = monthlyExpenses.multiply(BigDecimal.valueOf(6));

        BigDecimal liquidValue = assets.stream()
                .filter(a -> {
                    Product p = productMap.get(a.getProductId());
                    return p != null && p.getType() != null
                            && LIQUID_PRODUCT_TYPES.contains(p.getType().toLowerCase());
                })
                .map(a -> a.getCurrentValue() != null ? a.getCurrentValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int emergency;
        if (emergencyTarget.compareTo(BigDecimal.ZERO) > 0) {
            double ratio = liquidValue.doubleValue() / emergencyTarget.doubleValue();
            emergency = Math.min(25, (int) Math.round(ratio * 25));
        } else {
            emergency = 12; // No expenses → midpoint score
        }

        // ══════════════════════════════════════════════════════════════
        // COMPONENT 2: Diversification (25 pts)
        // Unique product types owned vs. eligible (visible + within risk)
        // ══════════════════════════════════════════════════════════════
        final int finalMaxRiskLv = maxRiskLv;

        Set<String> eligibleTypes = products.stream()
                .filter(p -> p.getVisible() != null && p.getVisible()
                        && p.getRiskLevel() != null && p.getRiskLevel() <= finalMaxRiskLv)
                .map(p -> p.getType() != null ? p.getType().toLowerCase() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> ownedTypes = assets.stream()
                .map(a -> {
                    Product p = productMap.get(a.getProductId());
                    return p != null && p.getType() != null ? p.getType().toLowerCase() : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        int diversification;
        if (!eligibleTypes.isEmpty()) {
            diversification = Math.min(25,
                    (int) Math.round((double) ownedTypes.size() / eligibleTypes.size() * 25));
        } else {
            diversification = 0;
        }

        // ══════════════════════════════════════════════════════════════
        // COMPONENT 3: Goal Coverage (25 pts)
        // Goals that have a matching product type in portfolio
        // ══════════════════════════════════════════════════════════════
        int goalCoverage;
        if (goals.isEmpty()) {
            goalCoverage = 12; // No goals → midpoint score
        } else {
            long coveredGoals = goals.stream()
                    .filter(g -> {
                        String goalType = g.getType() != null
                                ? g.getType().toLowerCase() : "custom";
                        List<String> suitableTypes = GOAL_PRODUCT_TYPES
                                .getOrDefault(goalType, GOAL_PRODUCT_TYPES.get("custom"));
                        return suitableTypes.stream()
                                .anyMatch(t -> ownedTypes.contains(t.toLowerCase()));
                    })
                    .count();
            goalCoverage = (int) Math.round((double) coveredGoals / goals.size() * 25);
        }

        // ══════════════════════════════════════════════════════════════
        // COMPONENT 4: Risk Alignment (25 pts)
        // Weighted avg portfolio risk vs. profile target
        // ══════════════════════════════════════════════════════════════
        int riskAlignment = 12; // default midpoint
        if (totalValue.compareTo(BigDecimal.ZERO) > 0) {
            double avgRisk = assets.stream()
                    .mapToDouble(a -> {
                        Product p = productMap.get(a.getProductId());
                        if (p != null && p.getRiskLevel() != null
                                && a.getCurrentValue() != null) {
                            return (a.getCurrentValue().doubleValue()
                                    / totalValue.doubleValue()) * p.getRiskLevel();
                        }
                        return 0.0;
                    })
                    .sum();

            double target = RISK_TARGETS.getOrDefault(
                    riskProfile != null ? riskProfile.toLowerCase() : "", 2.5);
            double diff = Math.abs(avgRisk - target);

            if (diff <= 0.5) {
                riskAlignment = 25;
            } else if (diff <= 1.0) {
                riskAlignment = 20;
            } else if (diff <= 1.5) {
                riskAlignment = 14;
            } else if (diff <= 2.0) {
                riskAlignment = 8;
            } else {
                riskAlignment = 4;
            }
        }

        // ── Aggregate score ──
        int totalScore = emergency + diversification + goalCoverage + riskAlignment;

        // ── Available surplus (monthly income – expenses) ──
        BigDecimal availableSurplus = monthlyIncome.subtract(monthlyExpenses);

        // ── Status label derived from total score ──
        String status;
        if (totalScore >= 80) {
            status = "Excellent";
        } else if (totalScore >= 60) {
            status = "Good";
        } else if (totalScore >= 40) {
            status = "Fair";
        } else {
            status = "Poor";
        }

        // ── Build component DTOs ──
        List<ComponentDTO> components = List.of(
                ComponentDTO.builder()
                        .componentName("emergency")
                        .label("Emergency Fund")
                        .score(emergency)
                        .maxScore(25)
                        .build(),
                ComponentDTO.builder()
                        .componentName("diversification")
                        .label("Diversification")
                        .score(diversification)
                        .maxScore(25)
                        .build(),
                ComponentDTO.builder()
                        .componentName("goalCoverage")
                        .label("Goal Coverage")
                        .score(goalCoverage)
                        .maxScore(25)
                        .build(),
                ComponentDTO.builder()
                        .componentName("riskAlignment")
                        .label("Risk Alignment")
                        .score(riskAlignment)
                        .maxScore(25)
                        .build()
        );

        return HealthDTO.builder()
                .totalScore(totalScore)
                .maxScore(100)
                .status(status)
                .portofolioValue(totalValue)
                .availableSurplus(availableSurplus)
                .components(components)
                .build();
    }
}

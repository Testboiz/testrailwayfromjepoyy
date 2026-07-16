package com.indivaragroup.jdt17wms.constants;

import com.indivaragroup.jdt17wms.dto.utils.ErrorResponseDTO;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class AppConstants {
    private AppConstants() {
        // Prevent instantiation
    }

  public static final String CUSTOM_GOAL = "custom";
  public static final String MEDIUM_PRIORITY = "medium";
  public static final String HIGH_PRIORITY = "high";
  public static final String LOW_PRIORITY = "low";

    public static final int DEFAULT_QUESTIONNAIRE_SIZE = 5;
    public static final int RISK_AVERSE_THRESHOLD = 3;
    public static final int RISK_MODERATE_THRESHOLD = 7;
    public static final int RISK_SCALING_FACTOR = 10;
    public static final int MIN_ANSWER_SCORE = 0;
    public static final int MAX_ANSWER_SCORE = 2;

    // Asset/Product Type Constants;typeLabels
    public static final String MONEY_MARKET = "money_market";
    public static final String DEPOSIT = "deposit";
    public static final String BOND = "bond";
    public static final String SUKUK = "sukuk";
    public static final String MUTUAL_FUND = "mutual_fund";
    public static final String BALANCED_FUND = "balanced_fund";
    public static final String STOCK = "stock";

    // ActionRecommendationService constants
    public static final Set<String> LIQUID_PRODUCT_TYPES = Set.of(MONEY_MARKET, DEPOSIT);
    public static final List<String> ALL_PRODUCT_TYPES = List.of(
            MONEY_MARKET, DEPOSIT, BOND, SUKUK, MUTUAL_FUND, BALANCED_FUND, STOCK
    );
    public static final Map<String, List<String>> GOAL_PRODUCT_TYPES = Map.of(
            "emergency_fund", List.of(MONEY_MARKET, DEPOSIT, BALANCED_FUND),
            "vacation", List.of(MONEY_MARKET, DEPOSIT, BALANCED_FUND),
            "vehicle_purchase", List.of(MONEY_MARKET, DEPOSIT, BALANCED_FUND, BOND, SUKUK),
            "property", List.of(BALANCED_FUND, BOND, SUKUK, STOCK),
            "retirement", List.of(STOCK, BOND, SUKUK, BALANCED_FUND, MONEY_MARKET, DEPOSIT),
            CUSTOM_GOAL, List.of(STOCK, MONEY_MARKET, BALANCED_FUND, BOND, SUKUK, DEPOSIT)
    );
    public static final Map<String, Integer> MAX_RISK_LEVELS = Map.of(
            "risk_averse", 2,
            "moderate", 4,
            "risk_taker", 5
    );
    public static final Map<String, Double> RISK_TARGETS = Map.of(
            "risk_averse", 1.5,
            "moderate", 2.5,
            "risk_taker", 4.0
    );
    public static final Map<String, String> TYPE_LABELS = Map.of(
            MONEY_MARKET, "Money Market",
            DEPOSIT, "Deposit",
            BALANCED_FUND, "Balanced Fund",
            MUTUAL_FUND, "Mutual Fund",
            BOND, "Bond",
            SUKUK, "Sukuk",
            STOCK, "Stock"
    );
    public static final BigDecimal SURPLUS_THRESHOLD = BigDecimal.valueOf(100000);

    public static final Map<String, Integer> GOAL_MAX_MONTHS = Map.of(
            "savings", 18,
            "vacation", 12,
            "vehicle", 48,
            "property", 120,
            "retirement", 420,
            CUSTOM_GOAL, 60
    );
    // Pre-constructed ErrorResponseDTOs
    public static final ErrorResponseDTO ERROR_UNAUTHORIZED = ErrorResponseDTO.builder()
            .error("Unauthorized")
            .code(401)
            .build();

    public static final ErrorResponseDTO ERROR_FORBIDDEN = ErrorResponseDTO.builder()
            .error("Forbidden")
            .code(403)
            .build();
}

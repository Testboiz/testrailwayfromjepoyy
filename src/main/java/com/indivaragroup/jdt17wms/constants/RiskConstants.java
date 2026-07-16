package com.indivaragroup.jdt17wms.constants;

import java.util.Map;

public final class RiskConstants {
    private RiskConstants() {}

    public static final int DEFAULT_QUESTIONNAIRE_SIZE = 5;
    public static final int RISK_AVERSE_THRESHOLD = 3;
    public static final int RISK_MODERATE_THRESHOLD = 7;
    public static final int RISK_SCALING_FACTOR = 10;
    public static final int MIN_ANSWER_SCORE = 0;
    public static final int MAX_ANSWER_SCORE = 2;

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
}

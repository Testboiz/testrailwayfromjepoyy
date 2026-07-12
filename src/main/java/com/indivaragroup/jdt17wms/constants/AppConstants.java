package com.indivaragroup.jdt17wms.constants;

import java.util.UUID;

public final class AppConstants {
    private AppConstants() {
        // Prevent instantiation
    }

    public static final UUID USER_ID = UUID.fromString("7957b44d-131c-4e84-8bb8-6b68ebde72d9");
    public static final int DEFAULT_QUESTIONNAIRE_SIZE = 5;
    public static final int RISK_AVERSE_THRESHOLD = 3;
    public static final int RISK_MODERATE_THRESHOLD = 7;
    public static final int RISK_SCALING_FACTOR = 10;
    public static final int MIN_ANSWER_SCORE = 0;
    public static final int MAX_ANSWER_SCORE = 2;
}

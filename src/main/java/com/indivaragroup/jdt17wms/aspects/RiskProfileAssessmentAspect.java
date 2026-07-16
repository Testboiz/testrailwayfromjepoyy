package com.indivaragroup.jdt17wms.aspects;

import com.indivaragroup.jdt17wms.dto.utils.ApiError;
import com.indivaragroup.jdt17wms.dto.utils.SecurityUtils;
import com.indivaragroup.jdt17wms.exceptions.CoreThrowHandler;
import com.indivaragroup.jdt17wms.models.User;
import com.indivaragroup.jdt17wms.models.enums.UserRole;
import com.indivaragroup.jdt17wms.repositories.UserRepository;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
public class RiskProfileAssessmentAspect {

    private final UserRepository userRepository;

    public RiskProfileAssessmentAspect(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Before("@annotation(com.indivaragroup.jdt17wms.aspects.RiskProfileAssessmentRequired)")
    public void checkRiskProfileAssessment() {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CoreThrowHandler(ApiError.USER_NOT_FOUND));

        if (user.getRole() != UserRole.ADMIN && !Boolean.TRUE.equals(user.getQuestionnaireCompleted())) {
            throw new CoreThrowHandler(ApiError.REQUIRED_RISK_PROFILER);
        }
    }
}

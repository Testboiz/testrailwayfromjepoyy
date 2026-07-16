package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.dto.utils.ApiError;
import com.indivaragroup.jdt17wms.dto.utils.SecurityUtils;
import com.indivaragroup.jdt17wms.exceptions.CoreThrowHandler;
import com.indivaragroup.jdt17wms.models.User;
import com.indivaragroup.jdt17wms.repositories.UserRepository;

public interface VerifiedUserProvider {
    UserRepository userRepository();

    default User getVerifiedUser() {
        User user = userRepository().findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new CoreThrowHandler(ApiError.USER_NOT_FOUND));
        if (!Boolean.TRUE.equals(user.getQuestionnaireCompleted())) {
            throw new CoreThrowHandler(ApiError.REQUIRED_RISK_PROFILER);
        }
        return user;
    }
}

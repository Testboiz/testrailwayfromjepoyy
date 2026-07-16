package com.indivaragroup.jdt17wms.aspects;

import com.indivaragroup.jdt17wms.dto.utils.ApiError;
import com.indivaragroup.jdt17wms.dto.utils.SecurityUtils;
import com.indivaragroup.jdt17wms.exceptions.CoreThrowHandler;
import com.indivaragroup.jdt17wms.models.User;
import com.indivaragroup.jdt17wms.models.enums.UserRole;
import com.indivaragroup.jdt17wms.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskProfileAssessmentAspectTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RiskProfileAssessmentAspect aspect;

    private MockedStatic<SecurityUtils> mockedSecurityUtils;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockedSecurityUtils = mockStatic(SecurityUtils.class);
        mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityUtils.close();
    }

    @Test
    void checkRiskProfileAssessment_userNotFound_throwsNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        CoreThrowHandler ex = assertThrows(CoreThrowHandler.class, () -> aspect.checkRiskProfileAssessment());
        assertEquals("User Not Found", ex.getMessage());
    }

    @Test
    void checkRiskProfileAssessment_questionnaireCompleted_doesNotThrow() {
        User user = User.builder()
                .id(userId)
                .role(UserRole.USER)
                .questionnaireCompleted(true)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> aspect.checkRiskProfileAssessment());
    }

    @Test
    void checkRiskProfileAssessment_questionnaireNotCompletedUser_throwsRequiredRiskProfiler() {
        User user = User.builder()
                .id(userId)
                .role(UserRole.USER)
                .questionnaireCompleted(false)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        CoreThrowHandler ex = assertThrows(CoreThrowHandler.class, () -> aspect.checkRiskProfileAssessment());
        assertEquals(ApiError.REQUIRED_RISK_PROFILER.getMessage(), ex.getMessage());
    }

    @Test
    void checkRiskProfileAssessment_questionnaireNotCompletedAdmin_doesNotThrow() {
        User user = User.builder()
                .id(userId)
                .role(UserRole.ADMIN)
                .questionnaireCompleted(false)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> aspect.checkRiskProfileAssessment());
    }
}

package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.constants.AppConstants;
import com.indivaragroup.jdt17wms.dto.response.GoalDTO;
import com.indivaragroup.jdt17wms.exceptions.MissingRiskProfileException;
import com.indivaragroup.jdt17wms.exceptions.NotFoundException;
import com.indivaragroup.jdt17wms.models.Goal;
import com.indivaragroup.jdt17wms.models.User;
import com.indivaragroup.jdt17wms.repositories.GoalRepository;
import com.indivaragroup.jdt17wms.repositories.UserRepository;
import com.indivaragroup.jdt17wms.dto.request.GoalRegistrationDTO;
import com.indivaragroup.jdt17wms.exceptions.DuplicatePriorityGoalException;
import com.indivaragroup.jdt17wms.dto.request.GoalEditingDTO;
import com.indivaragroup.jdt17wms.exceptions.InsufficientIncomeException;
import com.indivaragroup.jdt17wms.repositories.FinancialProfileRepository;
import com.indivaragroup.jdt17wms.models.FinancialProfile;
import com.indivaragroup.jdt17wms.repositories.AssetRepository;
import com.indivaragroup.jdt17wms.models.Asset;
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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GoalsManagementServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FinancialProfileRepository financialProfileRepository;

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private GoalsManagementService goalsManagementService;

    @Test
    void serviceShouldBeInitialized() {
        assertNotNull(goalsManagementService);
    }

    @Test
    void getGoalsForUser_shouldReturnGoalsWhenQuestionnaireCompleted() {
        User user = User.builder()
                .id(AppConstants.USER_ID)
                .questionnaireCompleted(true)
                .build();
        Goal goal = Goal.builder()
                .id(UUID.randomUUID())
                .userId(AppConstants.USER_ID)
                .name("Retirement Fund")
                .targetAmount(new java.math.BigDecimal("500000.00"))
                .build();

        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.of(user));
        when(goalRepository.findAllByUserId(AppConstants.USER_ID)).thenReturn(List.of(goal));

        List<GoalDTO> result = goalsManagementService.getGoalsForUser();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Retirement Fund", result.get(0).getName());
        assertEquals(new java.math.BigDecimal("500000.00"), result.get(0).getTargetAmount());
    }

    @Test
    void getGoalsForUser_shouldThrowMissingRiskProfileExceptionWhenQuestionnaireNotCompleted() {
        User user = User.builder()
                .id(AppConstants.USER_ID)
                .questionnaireCompleted(false)
                .build();

        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.of(user));

        assertThrows(MissingRiskProfileException.class, () -> goalsManagementService.getGoalsForUser());
    }

    @Test
    void getGoalsForUser_shouldThrowNotFoundExceptionWhenUserNotFound() {
        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> goalsManagementService.getGoalsForUser());
    }

    @Test
    void createGoalForUser_shouldCreateGoalSuccessfully() throws Exception {
        User user = User.builder()
                .id(AppConstants.USER_ID)
                .questionnaireCompleted(true)
                .build();
        GoalRegistrationDTO request = GoalRegistrationDTO.builder()
                .name("Retirement Fund")
                .type("retirement")
                .targetAmount(new java.math.BigDecimal("500000.00"))
                .monthlyContribution(new java.math.BigDecimal("1000.00"))
                .targetDate(java.time.LocalDate.of(2040, 1, 1))
                .isPriority(true)
                .build();

        Goal goal = Goal.builder()
                .id(UUID.randomUUID())
                .userId(AppConstants.USER_ID)
                .name("Retirement Fund")
                .type("retirement")
                .targetAmount(new java.math.BigDecimal("500000.00"))
                .status(com.indivaragroup.jdt17wms.models.enums.GoalStatus.IN_PROGRESS)
                .build();

        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.of(user));
        when(goalRepository.findAllByUserId(AppConstants.USER_ID)).thenReturn(List.of());
        when(goalRepository.save(any(Goal.class))).thenReturn(goal);

        GoalDTO result = goalsManagementService.createGoalForUser(request);

        assertNotNull(result);
        assertEquals("Retirement Fund", result.getName());
        assertEquals(new java.math.BigDecimal("500000.00"), result.getTargetAmount());
    }

    @Test
    void createGoalForUser_shouldThrowMissingRiskProfileExceptionWhenQuestionnaireNotCompleted() {
        User user = User.builder()
                .id(AppConstants.USER_ID)
                .questionnaireCompleted(false)
                .build();
        GoalRegistrationDTO request = GoalRegistrationDTO.builder().build();

        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.of(user));

        assertThrows(MissingRiskProfileException.class, () -> goalsManagementService.createGoalForUser(request));
    }

    @Test
    void createGoalForUser_shouldThrowDuplicatePriorityGoalExceptionWhenPriorityGoalAlreadyExists() {
        User user = User.builder()
                .id(AppConstants.USER_ID)
                .questionnaireCompleted(true)
                .build();
        GoalRegistrationDTO request = GoalRegistrationDTO.builder()
                .type("savings")
                .targetDate(LocalDate.now(java.time.ZoneOffset.UTC).plusMonths(1))
                .isPriority(true)
                .build();

        Goal existingGoal = Goal.builder()
                .isPriority(true)
                .status(com.indivaragroup.jdt17wms.models.enums.GoalStatus.IN_PROGRESS)
                .build();

        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.of(user));
        when(goalRepository.findAllByUserId(AppConstants.USER_ID)).thenReturn(List.of(existingGoal));

        assertThrows(DuplicatePriorityGoalException.class, () -> goalsManagementService.createGoalForUser(request));
    }

    @Test
    void updateGoalForUser_shouldUpdateGoalSuccessfully() throws Exception {
        UUID goalId = UUID.randomUUID();
        User user = User.builder()
                .id(AppConstants.USER_ID)
                .questionnaireCompleted(true)
                .build();
        Goal existingGoal = Goal.builder()
                .id(goalId)
                .userId(AppConstants.USER_ID)
                .name("Retirement Fund")
                .type("retirement")
                .targetAmount(new BigDecimal("500000.00"))
                .monthlyContribution(new BigDecimal("1000.00"))
                .isPriority(false)
                .status(com.indivaragroup.jdt17wms.models.enums.GoalStatus.IN_PROGRESS)
                .build();

        GoalEditingDTO request = GoalEditingDTO.builder()
                .name("New retirement")
                .targetAmount(new BigDecimal("600000.00"))
                .monthlyContribution(new BigDecimal("1200.00"))
                .targetDate(java.time.LocalDate.now(java.time.ZoneOffset.UTC).plusMonths(60))
                .isPriority(true)
                .notes("Updated notes")
                .build();

        FinancialProfile profile = FinancialProfile.builder()
                .monthlyIncome(new BigDecimal("5000.00"))
                .build();

        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.of(user));
        when(goalRepository.findById(goalId)).thenReturn(Optional.of(existingGoal));
        when(goalRepository.findAllByUserId(AppConstants.USER_ID)).thenReturn(List.of(existingGoal));
        when(financialProfileRepository.findByUserId(AppConstants.USER_ID)).thenReturn(Optional.of(profile));
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GoalDTO result = goalsManagementService.updateGoalForUser(goalId, request);

        assertNotNull(result);
        assertEquals("New retirement", result.getName());
        assertEquals(new BigDecimal("600000.00"), result.getTargetAmount());
        assertEquals(new BigDecimal("1200.00"), result.getMonthlyContribution());
        assertEquals(true, result.getIsPriority());
    }

    @Test
    void updateGoalForUser_shouldThrowMissingRiskProfileExceptionWhenQuestionnaireNotCompleted() {
        UUID goalId = UUID.randomUUID();
        User user = User.builder()
                .id(AppConstants.USER_ID)
                .questionnaireCompleted(false)
                .build();
        GoalEditingDTO request = GoalEditingDTO.builder().build();

        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.of(user));

        assertThrows(MissingRiskProfileException.class, () -> goalsManagementService.updateGoalForUser(goalId, request));
    }

    @Test
    void updateGoalForUser_shouldThrowNotFoundExceptionWhenGoalNotFound() {
        UUID goalId = UUID.randomUUID();
        User user = User.builder()
                .id(AppConstants.USER_ID)
                .questionnaireCompleted(true)
                .build();
        GoalEditingDTO request = GoalEditingDTO.builder().build();

        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.of(user));
        when(goalRepository.findById(goalId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> goalsManagementService.updateGoalForUser(goalId, request));
    }

    @Test
    void updateGoalForUser_shouldThrowDuplicatePriorityGoalExceptionWhenPriorityGoalAlreadyExists() {
        UUID goalId = UUID.randomUUID();
        User user = User.builder()
                .id(AppConstants.USER_ID)
                .questionnaireCompleted(true)
                .build();
        Goal existingGoal = Goal.builder()
                .id(goalId)
                .userId(AppConstants.USER_ID)
                .isPriority(false)
                .status(com.indivaragroup.jdt17wms.models.enums.GoalStatus.IN_PROGRESS)
                .build();
        Goal otherGoal = Goal.builder()
                .id(UUID.randomUUID())
                .userId(AppConstants.USER_ID)
                .isPriority(true)
                .status(com.indivaragroup.jdt17wms.models.enums.GoalStatus.IN_PROGRESS)
                .build();

        GoalEditingDTO request = GoalEditingDTO.builder()
                .isPriority(true)
                .build();

        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.of(user));
        when(goalRepository.findById(goalId)).thenReturn(Optional.of(existingGoal));
        when(goalRepository.findAllByUserId(AppConstants.USER_ID)).thenReturn(List.of(existingGoal, otherGoal));

        assertThrows(DuplicatePriorityGoalException.class, () -> goalsManagementService.updateGoalForUser(goalId, request));
    }

    @Test
    void updateGoalForUser_shouldThrowInsufficientIncomeExceptionWhenContributionExceedsIncome() {
        UUID goalId = UUID.randomUUID();
        User user = User.builder()
                .id(AppConstants.USER_ID)
                .questionnaireCompleted(true)
                .build();
        Goal existingGoal = Goal.builder()
                .id(goalId)
                .userId(AppConstants.USER_ID)
                .monthlyContribution(new BigDecimal("1000.00"))
                .isPriority(false)
                .status(com.indivaragroup.jdt17wms.models.enums.GoalStatus.IN_PROGRESS)
                .build();

        GoalEditingDTO request = GoalEditingDTO.builder()
                .monthlyContribution(new BigDecimal("6000.00")) // Exceeds income of 5000
                .build();

        FinancialProfile profile = FinancialProfile.builder()
                .monthlyIncome(new BigDecimal("5000.00"))
                .build();

        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.of(user));
        when(goalRepository.findById(goalId)).thenReturn(Optional.of(existingGoal));
        when(goalRepository.findAllByUserId(AppConstants.USER_ID)).thenReturn(List.of(existingGoal));
        when(financialProfileRepository.findByUserId(AppConstants.USER_ID)).thenReturn(Optional.of(profile));

        assertThrows(InsufficientIncomeException.class, () -> goalsManagementService.updateGoalForUser(goalId, request));
    }

    @Test
    void deleteGoalForUser_shouldDeleteGoalSuccessfully() {
        UUID goalId = UUID.randomUUID();
        User user = User.builder()
                .id(AppConstants.USER_ID)
                .questionnaireCompleted(true)
                .build();
        Goal goal = Goal.builder()
                .id(goalId)
                .userId(AppConstants.USER_ID)
                .build();
        Asset asset = Asset.builder()
                .id(UUID.randomUUID())
                .goalId(goalId)
                .build();

        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.of(user));
        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));
        when(assetRepository.findAllByGoalId(goalId)).thenReturn(List.of(asset));

        goalsManagementService.deleteGoalForUser(goalId);

        verify(assetRepository).save(asset);
        verify(goalRepository).delete(goal);
    }

    @Test
    void deleteGoalForUser_shouldThrowMissingRiskProfileExceptionWhenQuestionnaireNotCompleted() {
        UUID goalId = UUID.randomUUID();
        User user = User.builder()
                .id(AppConstants.USER_ID)
                .questionnaireCompleted(false)
                .build();

        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.of(user));

        assertThrows(MissingRiskProfileException.class, () -> goalsManagementService.deleteGoalForUser(goalId));
    }

    @Test
    void deleteGoalForUser_shouldThrowNotFoundExceptionWhenGoalNotFound() {
        UUID goalId = UUID.randomUUID();
        User user = User.builder()
                .id(AppConstants.USER_ID)
                .questionnaireCompleted(true)
                .build();

        when(userRepository.findById(AppConstants.USER_ID)).thenReturn(Optional.of(user));
        when(goalRepository.findById(goalId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> goalsManagementService.deleteGoalForUser(goalId));
    }
}

package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.constants.AppConstants;
import com.indivaragroup.jdt17wms.dto.utils.SecurityUtils;
import com.indivaragroup.jdt17wms.dto.response.GoalDTO;
import com.indivaragroup.jdt17wms.dto.request.GoalRegistrationDTO;
import com.indivaragroup.jdt17wms.exceptions.DuplicatePriorityGoalException;
import com.indivaragroup.jdt17wms.exceptions.MissingRiskProfileException;
import com.indivaragroup.jdt17wms.exceptions.NotFoundException;
import com.indivaragroup.jdt17wms.models.FinancialProfile;
import com.indivaragroup.jdt17wms.models.Goal;
import com.indivaragroup.jdt17wms.models.User;
import com.indivaragroup.jdt17wms.models.Asset;
import com.indivaragroup.jdt17wms.repositories.GoalRepository;
import com.indivaragroup.jdt17wms.repositories.UserRepository;
import com.indivaragroup.jdt17wms.repositories.FinancialProfileRepository;
import com.indivaragroup.jdt17wms.repositories.AssetRepository;
import com.indivaragroup.jdt17wms.dto.request.GoalEditingDTO;
import com.indivaragroup.jdt17wms.exceptions.InsufficientIncomeException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class GoalsManagementService {

    private static final String FIELD_TYPE = "type";
    private static final String FIELD_TARGET_DATE = "targetDate";
    private static final String ERROR_CODE_INVALID = "invalid";

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final FinancialProfileRepository financialProfileRepository;
    private final AssetRepository assetRepository;
    private final Clock clock;

    public GoalsManagementService(GoalRepository goalRepository, UserRepository userRepository, FinancialProfileRepository financialProfileRepository, AssetRepository assetRepository, Clock clock) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
        this.financialProfileRepository = financialProfileRepository;
        this.assetRepository = assetRepository;
        this.clock = clock;
    }

    public List<GoalDTO> getGoalsForUser() {
        User user = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getQuestionnaireCompleted() == null || !user.getQuestionnaireCompleted()) {
            throw new MissingRiskProfileException("Risk Profiler Assessment Required");
        }

        List<Goal> goals = goalRepository.findAllByUserId(user.getId());

        return goals.stream()
                .map(goal -> GoalDTO.builder()
                        .id(goal.getId())
                        .userId(goal.getUserId())
                        .name(goal.getName())
                        .type(goal.getType())
                        .targetAmount(goal.getTargetAmount())
                        .monthlyContribution(goal.getMonthlyContribution())
                        .targetDate(goal.getTargetDate())
                        .isPriority(goal.getIsPriority())
                        .notes(goal.getNotes())
                        .status(goal.getStatus())
                        .createdAt(goal.getCreatedAt())
                        .updatedAt(goal.getUpdatedAt())
                        .build())
                .toList();
    }

    public GoalDTO createGoalForUser(GoalRegistrationDTO dto) throws BindException {
        User user = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getQuestionnaireCompleted() == null || !user.getQuestionnaireCompleted()) {
            throw new MissingRiskProfileException("Risk Profiler Assessment Required");
        }

        BindException bindException = new BindException(dto, "goalRegistrationDTO");
        String rawType = dto.getType();
        String normalizedType = rawType != null ? rawType.trim().toLowerCase() : "";
        if (!AppConstants.GOAL_MAX_MONTHS.containsKey(normalizedType)) {
            bindException.rejectValue(FIELD_TYPE, ERROR_CODE_INVALID, "Invalid goal type");
        }

        if (dto.getTargetDate() != null) {
            LocalDate now = LocalDate.now(clock);
            if (dto.getTargetDate().isBefore(now)) {
                bindException.rejectValue(FIELD_TARGET_DATE, ERROR_CODE_INVALID, "Target date must be in the future");
            } else if (AppConstants.GOAL_MAX_MONTHS.containsKey(normalizedType)) {
                int maxMonths = AppConstants.GOAL_MAX_MONTHS.get(normalizedType);
                long months = ChronoUnit.MONTHS.between(now, dto.getTargetDate());
                if (months > maxMonths) {
                    bindException.rejectValue(FIELD_TARGET_DATE, ERROR_CODE_INVALID, "Target date exceeds maximum limit of " + maxMonths + " months");
                }
            }
        }

        if (bindException.hasErrors()) {
            throw bindException;
        }

        if (Boolean.TRUE.equals(dto.getIsPriority())) {
            boolean hasPriorityGoal = goalRepository.findAllByUserId(user.getId()).stream()
                    .anyMatch(g -> Boolean.TRUE.equals(g.getIsPriority()) && g.getStatus() == com.indivaragroup.jdt17wms.models.enums.GoalStatus.IN_PROGRESS);
            if (hasPriorityGoal) {
                throw new DuplicatePriorityGoalException("Can’t set more than 1 priority");
            }
        }

        Goal goal = Goal.builder()
                .userId(user.getId())
                .name(dto.getName())
                .type(dto.getType())
                .targetAmount(dto.getTargetAmount())
                .currentAmount(BigDecimal.ZERO) // Set initial value as 0
                .monthlyContribution(dto.getMonthlyContribution())
                .targetDate(dto.getTargetDate())
                .isPriority(dto.getIsPriority() != null && dto.getIsPriority())
                .notes(dto.getNotes())
                .status(com.indivaragroup.jdt17wms.models.enums.GoalStatus.IN_PROGRESS)
                .build();

        goal = goalRepository.save(goal);

        return GoalDTO.builder()
                .id(goal.getId())
                .userId(goal.getUserId())
                .name(goal.getName())
                .type(goal.getType())
                .targetAmount(goal.getTargetAmount())
                .monthlyContribution(goal.getMonthlyContribution())
                .targetDate(goal.getTargetDate())
                .isPriority(goal.getIsPriority())
                .notes(goal.getNotes())
                .status(goal.getStatus())
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }

    public GoalDTO updateGoalForUser(UUID goalId, GoalEditingDTO dto) throws BindException {
        User user = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getQuestionnaireCompleted() == null || !user.getQuestionnaireCompleted()) {
            throw new MissingRiskProfileException("Risk Profiler Assessment Required");
        }

        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new NotFoundException("No valid item with the ID"));

        BindException bindException = new BindException(dto, "goalEditingDTO");
        if (dto.getTargetDate() != null) {
            LocalDate now = LocalDate.now(clock);
            if (dto.getTargetDate().isBefore(now)) {
                bindException.rejectValue(FIELD_TARGET_DATE, ERROR_CODE_INVALID, "Target date must be in the future");
            } else {
                String normalizedType = goal.getType() != null ? goal.getType().trim().toLowerCase() : "custom";
                int maxMonths = AppConstants.GOAL_MAX_MONTHS.getOrDefault(normalizedType, 60);
                long months = ChronoUnit.MONTHS.between(now, dto.getTargetDate());
                if (months > maxMonths) {
                    bindException.rejectValue(FIELD_TARGET_DATE, ERROR_CODE_INVALID, "Target date exceeds maximum limit of " + maxMonths + " months");
                }
            }
        }

        if (bindException.hasErrors()) {
            throw bindException;
        }

        if (Boolean.TRUE.equals(dto.getIsPriority()) && !Boolean.TRUE.equals(goal.getIsPriority())) {
            boolean hasPriorityGoal = goalRepository.findAllByUserId(user.getId()).stream()
                    .anyMatch(g -> !g.getId().equals(goalId) && Boolean.TRUE.equals(g.getIsPriority()) && g.getStatus() == com.indivaragroup.jdt17wms.models.enums.GoalStatus.IN_PROGRESS);
            if (hasPriorityGoal) {
                throw new DuplicatePriorityGoalException("Can’t set more than 1 priority");
            }
        }

        BigDecimal monthlyIncome = financialProfileRepository.findByUserId(user.getId())
                .map(FinancialProfile::getMonthlyIncome)
                .orElse(BigDecimal.ZERO);

        BigDecimal totalContribution = goalRepository.findAllByUserId(user.getId()).stream()
                .map(g -> g.getId().equals(goalId) ? dto.getMonthlyContribution() : g.getMonthlyContribution())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalContribution.compareTo(monthlyIncome) > 0) {
            throw new InsufficientIncomeException("Can’t set more allocation than income");
        }

        goal.setName(dto.getName());
        goal.setTargetAmount(dto.getTargetAmount());
        goal.setMonthlyContribution(dto.getMonthlyContribution());
        goal.setTargetDate(dto.getTargetDate());
        goal.setIsPriority(dto.getIsPriority() != null && dto.getIsPriority());
        goal.setNotes(dto.getNotes());

        goal = goalRepository.save(goal);

        return GoalDTO.builder()
                .id(goal.getId())
                .userId(goal.getUserId())
                .name(goal.getName())
                .type(goal.getType())
                .targetAmount(goal.getTargetAmount())
                .monthlyContribution(goal.getMonthlyContribution())
                .targetDate(goal.getTargetDate())
                .isPriority(goal.getIsPriority())
                .notes(goal.getNotes())
                .status(goal.getStatus())
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }

    @Transactional
    public void deleteGoalForUser(UUID goalId) {
        User user = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getQuestionnaireCompleted() == null || !user.getQuestionnaireCompleted()) {
            throw new MissingRiskProfileException("Risk Profiler Assessment Required");
        }

        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new NotFoundException("No valid item with the ID"));

        List<Asset> assets = assetRepository.findAllByGoalId(goalId);
        for (Asset asset : assets) {
            asset.setGoalId(null);
            assetRepository.save(asset);
        }

        goalRepository.delete(goal);
    }
}

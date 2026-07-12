package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.constants.AppConstants;
import com.indivaragroup.jdt17wms.dto.response.GoalDTO;
import com.indivaragroup.jdt17wms.dto.request.GoalRegistrationDTO;
import com.indivaragroup.jdt17wms.exceptions.DuplicatePriorityGoalException;
import com.indivaragroup.jdt17wms.exceptions.MissingRiskProfileException;
import com.indivaragroup.jdt17wms.exceptions.NotFoundException;
import com.indivaragroup.jdt17wms.models.Goal;
import com.indivaragroup.jdt17wms.models.User;
import com.indivaragroup.jdt17wms.repositories.GoalRepository;
import com.indivaragroup.jdt17wms.repositories.UserRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoalsManagementService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    public GoalsManagementService(GoalRepository goalRepository, UserRepository userRepository) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
    }

    public List<GoalDTO> getGoalsForUser() {
        User user = userRepository.findById(AppConstants.USER_ID)
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
                .collect(Collectors.toList());
    }

    public GoalDTO createGoalForUser(GoalRegistrationDTO dto) {
        User user = userRepository.findById(AppConstants.USER_ID)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getQuestionnaireCompleted() == null || !user.getQuestionnaireCompleted()) {
            throw new MissingRiskProfileException("Risk Profiler Assessment Required");
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
                .isPriority(dto.getIsPriority() != null ? dto.getIsPriority() : false)
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
}

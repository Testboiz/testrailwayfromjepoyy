package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.constants.AppConstants;
import com.indivaragroup.jdt17wms.dto.response.GoalDTO;
import com.indivaragroup.jdt17wms.exceptions.MissingRiskProfileException;
import com.indivaragroup.jdt17wms.exceptions.NotFoundException;
import com.indivaragroup.jdt17wms.models.Goal;
import com.indivaragroup.jdt17wms.models.User;
import com.indivaragroup.jdt17wms.repositories.GoalRepository;
import com.indivaragroup.jdt17wms.repositories.UserRepository;
import org.springframework.stereotype.Service;

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
}

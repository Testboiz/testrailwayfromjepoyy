package com.indivaragroup.jdt17wms.controllers;

import com.indivaragroup.jdt17wms.dto.response.GoalDTO;
import com.indivaragroup.jdt17wms.services.GoalsManagementService;
import com.indivaragroup.jdt17wms.services.GoalsProjectionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import com.indivaragroup.jdt17wms.dto.request.GoalRegistrationDTO;
import jakarta.validation.Valid;

import com.indivaragroup.jdt17wms.dto.request.GoalEditingDTO;

import org.springframework.http.HttpStatus;

@RestController
public class GoalController {

    private final GoalsManagementService goalsManagementService;
    private final GoalsProjectionService goalsProjectionService;

    public GoalController(GoalsManagementService goalsManagementService,
                          GoalsProjectionService goalsProjectionService) {
        this.goalsManagementService = goalsManagementService;
        this.goalsProjectionService = goalsProjectionService;
    }

    @GetMapping("/api/v1/me/goals")
    public List<GoalDTO> getGoals() {
        return goalsManagementService.getGoalsForUser();
    }

    @PostMapping("/api/v1/me/goals")
    public GoalDTO createGoal(@Valid @RequestBody GoalRegistrationDTO goalRegistrationDTO) {
        return goalsManagementService.createGoalForUser(goalRegistrationDTO);
    }

    @PutMapping({"/api/v1/me/goals/{id}"})
    public GoalDTO updateGoal(@PathVariable UUID id, @Valid @RequestBody GoalEditingDTO goalEditingDTO) {
        return goalsManagementService.updateGoalForUser(id, goalEditingDTO);
    }

    @DeleteMapping({"/api/v1/me/goals/{id}"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGoal(@PathVariable UUID id) {
        goalsManagementService.deleteGoalForUser(id);
    }

    @GetMapping("/api/v1/me/goals/projections")
    public void getGoalProjections() {
    }
}

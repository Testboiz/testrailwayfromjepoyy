package com.indivaragroup.jdt17wms.controllers;

import com.indivaragroup.jdt17wms.services.GoalsManagementService;
import com.indivaragroup.jdt17wms.services.GoalsProjectionService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
    public void getGoals() {
    }

    @PostMapping("/api/v1/me/goals")
    public void createGoal() {
    }

    @PutMapping({"/api/v1/me/goals/{id}"})
    public void updateGoal(@PathVariable UUID id) {
    }

    @DeleteMapping({"/api/v1/me/goals/{id}"})
    public void deleteGoal(@PathVariable UUID id) {
    }

    @GetMapping("/api/v1/me/goals/projections")
    public void getGoalProjections() {
    }
}

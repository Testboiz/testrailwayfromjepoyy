package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.repositories.GoalRepository;
import org.springframework.stereotype.Service;

@Service
public class GoalsManagementService {

    private final GoalRepository goalRepository;

    public GoalsManagementService(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }
}

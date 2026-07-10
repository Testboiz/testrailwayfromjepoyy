package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.repositories.GoalRepository;
import org.springframework.stereotype.Service;

@Service
public class GoalsProjectionService {

    private final GoalRepository goalRepository;

    public GoalsProjectionService(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }
}

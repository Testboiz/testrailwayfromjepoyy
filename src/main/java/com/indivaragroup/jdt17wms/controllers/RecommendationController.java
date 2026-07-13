package com.indivaragroup.jdt17wms.controllers;

import com.indivaragroup.jdt17wms.dto.response.HealthDTO;
import com.indivaragroup.jdt17wms.dto.response.RecommendationDTO;
import com.indivaragroup.jdt17wms.services.ActionRecommendationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/me")
public class RecommendationController {

    private final ActionRecommendationService actionRecommendationService;

  public RecommendationController(ActionRecommendationService actionRecommendationService) {
        this.actionRecommendationService = actionRecommendationService;
  }

    @GetMapping("/health")
    public HealthDTO getHealth() {
        return actionRecommendationService.getHealthScore();
    }

    @PostMapping("/recommendations")
    public List<RecommendationDTO> getRecommendations() {
        return actionRecommendationService.generateRecommendations();
    }
}

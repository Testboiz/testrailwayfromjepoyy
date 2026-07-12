package com.indivaragroup.jdt17wms.controllers;

import com.indivaragroup.jdt17wms.dto.response.HealthDTO;
import com.indivaragroup.jdt17wms.services.ActionRecommendationService;
import com.indivaragroup.jdt17wms.services.ProductRecommendationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
public class RecommendationController {

    private final ActionRecommendationService actionRecommendationService;
    private final ProductRecommendationService productRecommendationService;

    public RecommendationController(ActionRecommendationService actionRecommendationService,
                                    ProductRecommendationService productRecommendationService) {
        this.actionRecommendationService = actionRecommendationService;
        this.productRecommendationService = productRecommendationService;
    }

    @GetMapping("/health")
    public HealthDTO getHealth() {
        return actionRecommendationService.getHealthScore();
    }

    @GetMapping("/recommendations")
    public void getRecommendations() {
    }
}

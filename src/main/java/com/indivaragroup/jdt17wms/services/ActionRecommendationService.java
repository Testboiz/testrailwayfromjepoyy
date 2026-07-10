package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.repositories.RecommendationRepository;
import org.springframework.stereotype.Service;

@Service
public class ActionRecommendationService {

    private final RecommendationRepository recommendationRepository;

    public ActionRecommendationService(RecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }
}

package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.repositories.RecommendationRepository;
import com.indivaragroup.jdt17wms.repositories.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductRecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final ProductRepository productRepository;

    public ProductRecommendationService(RecommendationRepository recommendationRepository,
                                        ProductRepository productRepository) {
        this.recommendationRepository = recommendationRepository;
        this.productRepository = productRepository;
    }
}

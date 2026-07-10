package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.repositories.RecommendationRepository;
import com.indivaragroup.jdt17wms.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class ProductRecommendationServiceTest {

    @Mock
    private RecommendationRepository recommendationRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductRecommendationService productRecommendationService;

    @Test
    void serviceShouldBeInitialized() {
        assertNotNull(productRecommendationService);
    }
}

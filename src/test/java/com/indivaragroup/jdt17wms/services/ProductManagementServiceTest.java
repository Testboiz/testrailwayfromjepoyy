package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.models.Product;
import com.indivaragroup.jdt17wms.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductManagementServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductManagementService productManagementService;

    @Test
    void serviceShouldBeInitialized() {
        assertNotNull(productManagementService);
    }

    @Test
    void getAllProducts_shouldReturnPaginatedProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Product product = new Product();
        Page<Product> expectedPage = new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.findAll(any(Pageable.class))).thenReturn(expectedPage);

        Page<Product> actualPage = productManagementService.getAllProducts(pageable);

        assertNotNull(actualPage);
        assertEquals(1, actualPage.getTotalElements());
        assertEquals(product, actualPage.getContent().get(0));
    }
}


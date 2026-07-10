package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.repositories.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductManagementService {

    private final ProductRepository productRepository;

    public ProductManagementService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
}

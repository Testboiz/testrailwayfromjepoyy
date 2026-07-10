package com.indivaragroup.jdt17wms.controllers;

import com.indivaragroup.jdt17wms.models.Product;
import com.indivaragroup.jdt17wms.services.ProductManagementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductManagementService productManagementService;

    public ProductController(ProductManagementService productManagementService) {
        this.productManagementService = productManagementService;
    }

    @GetMapping
    public Page<Product> getAllProducts(Pageable pageable) {
        return productManagementService.getAllProducts(pageable);
    }

    @PutMapping("/{id}")
    public void updateProduct(@PathVariable UUID id) {

    }
}


package com.indivaragroup.jdt17wms.controllers;

import com.indivaragroup.jdt17wms.services.ProductManagementService;
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
    public void getAllProducts() {
    }

    @PutMapping("/{id}")
    public void updateProduct(@PathVariable UUID id) {
    }
}

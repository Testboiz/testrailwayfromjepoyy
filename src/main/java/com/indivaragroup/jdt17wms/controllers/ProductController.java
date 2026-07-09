package com.indivaragroup.jdt17wms.controllers;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    @GetMapping
    public void getAllProducts() {
    }

    @PutMapping("/{id}")
    public void updateProduct(@PathVariable UUID id) {
    }
}

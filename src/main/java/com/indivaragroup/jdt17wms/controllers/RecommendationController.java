package com.indivaragroup.jdt17wms.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
public class RecommendationController {

    @GetMapping("/health")
    public void getHealth() {
    }

    @GetMapping("/recommendations")
    public void getRecommendations() {
    }
}

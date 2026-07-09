package com.indivaragroup.jdt17wms.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/profiler")
public class RiskProfilerAssessmentController {

    @GetMapping
    public void getProfilerAssessment() {
    }

    @PutMapping
    public void updateProfilerAssessment() {
    }
}

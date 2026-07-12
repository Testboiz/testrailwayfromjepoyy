package com.indivaragroup.jdt17wms.controllers;

import com.indivaragroup.jdt17wms.dto.request.RiskProfilerDTO;
import com.indivaragroup.jdt17wms.dto.response.QuestionnaireDTO;
import com.indivaragroup.jdt17wms.dto.response.RiskProfilerResponseDTO;
import com.indivaragroup.jdt17wms.services.RiskProfilerAssessmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/me/profiler")
public class RiskProfilerAssessmentController {

    private final RiskProfilerAssessmentService riskProfilerAssessmentService;

    public RiskProfilerAssessmentController(RiskProfilerAssessmentService riskProfilerAssessmentService) {
        this.riskProfilerAssessmentService = riskProfilerAssessmentService;
    }

    @GetMapping
    public List<QuestionnaireDTO> getProfilerAssessment() {
        return riskProfilerAssessmentService.getQuestionnaire();
    }

    @PutMapping
    public RiskProfilerResponseDTO updateProfilerAssessment(@RequestBody RiskProfilerDTO riskProfilerDTO) {
        return riskProfilerAssessmentService.updateProfilerAssessment(riskProfilerDTO);
    }
}

package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.repositories.FinancialProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class RiskProfilerAssessmentService {

    private final FinancialProfileRepository financialProfileRepository;

    public RiskProfilerAssessmentService(FinancialProfileRepository financialProfileRepository) {
        this.financialProfileRepository = financialProfileRepository;
    }
}

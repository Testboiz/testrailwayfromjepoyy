package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.repositories.FinancialProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class RiskProfilerAssessmentServiceTest {

    @Mock
    private FinancialProfileRepository financialProfileRepository;

    @InjectMocks
    private RiskProfilerAssessmentService riskProfilerAssessmentService;

    @Test
    void serviceShouldBeInitialized() {
        assertNotNull(riskProfilerAssessmentService);
    }
}

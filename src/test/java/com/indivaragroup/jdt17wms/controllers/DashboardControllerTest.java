package com.indivaragroup.jdt17wms.controllers;

import com.indivaragroup.jdt17wms.dto.response.*;
import com.indivaragroup.jdt17wms.dto.utils.ApiError;
import com.indivaragroup.jdt17wms.exceptions.CoreThrowHandler;
import com.indivaragroup.jdt17wms.services.DashboardService;
import com.indivaragroup.jdt17wms.services.JwtService;
import com.indivaragroup.jdt17wms.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void getAdminDashboard_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/admin-dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    void getUserDashboard_shouldReturnOk() throws Exception {
        UserDashboardDTO userDashboardDTO = UserDashboardDTO.builder()
                .portofolio(PortfolioDTO.builder()
                        .value("1000.00")
                        .invested("800.00")
                        .holdings(1)
                        .items(List.of(PortfolioItemDTO.builder().name("Product A").value(new BigDecimal("1000.00")).build()))
                        .build())
                .performance(List.of(PerformanceDTO.builder().month(1).value(new BigDecimal("1000.00")).build()))
                .build();

        when(dashboardService.getUserDashboard()).thenReturn(userDashboardDTO);

        mockMvc.perform(get("/api/v1/me/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portofolio.value").value("1000.00"))
                .andExpect(jsonPath("$.portofolio.invested").value("800.00"))
                .andExpect(jsonPath("$.performance[0].month").value(1))
                .andExpect(jsonPath("$.performance[0].value").value(1000.00));
    }

    @Test
    void getUserDashboard_shouldReturn422WhenQuestionnaireNotCompleted() throws Exception {
        doThrow(new CoreThrowHandler(ApiError.REQUIRED_RISK_PROFILER, "Risk Profiler Assessment Required"))
                .when(dashboardService).getUserDashboard();

        mockMvc.perform(get("/api/v1/me/dashboard"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Risk Profiler Assessment Required"))
                .andExpect(jsonPath("$.code").value(422));
    }
}

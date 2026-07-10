package com.indivaragroup.jdt17wms.controllers;

import com.indivaragroup.jdt17wms.services.AuditTrailManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditTrailManagementService auditTrailManagementService;

    @Test
    void getAuditLogs_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/audit"))
                .andExpect(status().isOk());
    }
}

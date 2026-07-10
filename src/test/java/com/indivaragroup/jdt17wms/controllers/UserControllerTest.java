package com.indivaragroup.jdt17wms.controllers;

import com.indivaragroup.jdt17wms.models.User;
import com.indivaragroup.jdt17wms.services.UserManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserManagementService userManagementService;

    @Test
    void getAllUsers_shouldReturnOk() throws Exception {
        Page<User> expectedPage = new PageImpl<>(List.of());
        when(userManagementService.getAllUsers(any(Pageable.class))).thenReturn(expectedPage);

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk());
    }


    @Test
    void updateUser_shouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(put("/api/v1/users/" + id)
                        .contentType("application/json")
                        .content("{\"status\":\"active\"}"))
                .andExpect(status().isOk());
    }
}


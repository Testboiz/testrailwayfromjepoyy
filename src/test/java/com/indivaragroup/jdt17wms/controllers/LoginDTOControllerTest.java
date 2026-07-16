package com.indivaragroup.jdt17wms.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indivaragroup.jdt17wms.dto.request.RegisterDTO;
import com.indivaragroup.jdt17wms.dto.utils.ApiError;
import com.indivaragroup.jdt17wms.exceptions.CoreThrowHandler;
import com.indivaragroup.jdt17wms.services.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class RegisterControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    // 📝 REGISTER — success
    @Test
    void register_withValidData_shouldReturnSuccess() throws Exception {
        doNothing().when(authService).register(any(RegisterDTO.class));

        String body = objectMapper.writeValueAsString(RegisterDTO.builder().registerRequestName("Test User").registerRequestEmail("test@example.com").registerRequestPassword("Test1234!").build());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    // 📝 REGISTER — validation error (e.g. invalid fields)
    @Test
    void register_withInvalidData_shouldReturnValidationError() throws Exception {
        doThrow(new CoreThrowHandler(ApiError.VALIDATION)).when(authService).register(any(RegisterDTO.class));

        String body = objectMapper.writeValueAsString(RegisterDTO.builder().registerRequestName("Test User").registerRequestEmail("invalid-email").registerRequestPassword("Test1234!").build());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("INVALID FIELD VALUES"))
                .andExpect(jsonPath("$.code").value(400));
    }

    // 📝 REGISTER — duplicate email (conflict error)
    @Test
    void register_withDuplicateEmail_shouldReturnConflictError() throws Exception {
        doThrow(new CoreThrowHandler(ApiError.NOT_UNIQUE_EMAIL)).when(authService).register(any(RegisterDTO.class));

        String body = objectMapper.writeValueAsString(RegisterDTO.builder().registerRequestName("Test User").registerRequestEmail("duplicate@example.com").registerRequestPassword("Test1234!").build());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email Already Used"))
                .andExpect(jsonPath("$.code").value(409));
    }

    // 📝 REGISTER — null request body
    @Test
    void register_withNullBody_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid Request Body"))
                .andExpect(jsonPath("$.code").value(400));
    }
}

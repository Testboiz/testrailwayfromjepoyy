package com.indivaragroup.jdt17wms.config;

import com.indivaragroup.jdt17wms.dto.utils.UserSecurityProjection;
import com.indivaragroup.jdt17wms.models.enums.UserRole;
import com.indivaragroup.jdt17wms.repositories.*;
import com.indivaragroup.jdt17wms.services.DashboardService;
import com.indivaragroup.jdt17wms.services.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
@AutoConfigureMockMvc
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssetRepository assetRepository;

    @MockBean
    private AuditLogRepository auditLogRepository;

    @MockBean
    private ExpenseRepository expenseRepository;

    @MockBean
    private FinancialProfileRepository financialProfileRepository;

    @MockBean
    private GoalRepository goalRepository;

    @MockBean
    private ProductPriceRepository productPriceRepository;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private RecommendationRepository recommendationRepository;

    @MockBean
    private TransactionHistoryRepository transactionHistoryRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private DashboardService dashboardService;

    @Test
    void whenUnauthenticated_accessingAdminDashboard_shouldReturn401Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin-dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void whenUnauthenticated_accessingUserDashboard_shouldReturn401Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/me/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void whenUnauthenticated_accessingShorthandUserDashboard_shouldReturn401Unauthorized() throws Exception {
        mockMvc.perform(get("/me/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void whenUnauthenticated_accessingShorthandLogin_shouldBypassFiltersAndReturn404NotFound() throws Exception {
        // Shorthand login should bypass security filters.
        // It will fail at the controller layer with 404 Not Found since only /api/v1/auth/login is mapped.
        // It should NOT be blocked with a 401/403 security exception.
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenUnauthenticated_accessingShorthandLoginWithExpiredToken_shouldBypassFiltersAndReturn404NotFound() throws Exception {
        // Bypassing filter with expired token on auth paths: should yield 404, not 401.
        String expiredToken = "expired-token";
        when(jwtService.isAccessToken(expiredToken)).thenReturn(false);

        mockMvc.perform(post("/auth/login")
                        .header("Authorization", "Bearer " + expiredToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenAuthenticatedAsUser_accessingAdminDashboard_shouldReturn403Forbidden() throws Exception {
        String token = "valid-user-token";

        when(jwtService.isAccessToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn("user@example.com");
        when(jwtService.getRoleFromToken(token)).thenReturn("ROLE_USER");

        UserSecurityProjection projection = mock(UserSecurityProjection.class);
        when(projection.getEmail()).thenReturn("user@example.com");
        when(projection.getRole()).thenReturn(UserRole.USER);
        when(projection.getPriorCount()).thenReturn(1L);

        when(userRepository.findUserSecurityProjectionByEmail("user@example.com"))
                .thenReturn(java.util.Optional.of(projection));

        mockMvc.perform(get("/api/v1/admin-dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void whenAuthenticatedAsUser_accessingUserDashboard_shouldReturnOk() throws Exception {
        String token = "valid-user-token";

        when(jwtService.isAccessToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn("user@example.com");
        when(jwtService.getRoleFromToken(token)).thenReturn("ROLE_USER");

        UserSecurityProjection projection = mock(UserSecurityProjection.class);
        when(projection.getEmail()).thenReturn("user@example.com");
        when(projection.getRole()).thenReturn(UserRole.USER);
        when(projection.getPriorCount()).thenReturn(1L);

        when(userRepository.findUserSecurityProjectionByEmail("user@example.com"))
                .thenReturn(java.util.Optional.of(projection));

        mockMvc.perform(get("/api/v1/me/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void whenAuthenticatedAsUser_accessingShorthandUserDashboard_shouldBypassFiltersAndReturn404NotFound() throws Exception {
        String token = "valid-user-token";

        when(jwtService.isAccessToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn("user@example.com");
        when(jwtService.getRoleFromToken(token)).thenReturn("ROLE_USER");

        UserSecurityProjection projection = mock(UserSecurityProjection.class);
        when(projection.getEmail()).thenReturn("user@example.com");
        when(projection.getRole()).thenReturn(UserRole.USER);
        when(projection.getPriorCount()).thenReturn(1L);

        when(userRepository.findUserSecurityProjectionByEmail("user@example.com"))
                .thenReturn(java.util.Optional.of(projection));

        mockMvc.perform(get("/me/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}

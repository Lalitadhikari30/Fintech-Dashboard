package com.fintech.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.dashboard.dto.request.FinancialRecordRequest;
import com.fintech.dashboard.dto.request.LoginRequest;
import com.fintech.dashboard.model.enums.RecordType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Dashboard analytics endpoints.
 * Verifies summary, category breakdown, recent transactions, and monthly trends.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String adminToken;

    private String loginAndGetToken(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("data").get("token").asText();
    }

    @BeforeEach
    void setUp() throws Exception {
        if (adminToken == null) {
            adminToken = loginAndGetToken("admin@fintech.com", "admin123");

            // Seed some financial records for dashboard tests
            FinancialRecordRequest income = new FinancialRecordRequest(
                    new BigDecimal("5000"), RecordType.INCOME, "Salary",
                    LocalDate.of(2024, 6, 1), "June salary"
            );
            FinancialRecordRequest expense = new FinancialRecordRequest(
                    new BigDecimal("1500"), RecordType.EXPENSE, "Rent",
                    LocalDate.of(2024, 6, 5), "June rent"
            );

            mockMvc.perform(post("/api/v1/records")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(income)));

            mockMvc.perform(post("/api/v1/records")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(expense)));
        }
    }

    @Test
    @Order(1)
    void getSummary_asAdmin_returns200WithAllFields() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/summary")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalIncome").isNumber())
                .andExpect(jsonPath("$.data.totalExpenses").isNumber())
                .andExpect(jsonPath("$.data.netBalance").isNumber())
                .andExpect(jsonPath("$.data.categoryBreakdown").isArray())
                .andExpect(jsonPath("$.data.recentTransactions").isArray());
    }

    @Test
    @Order(2)
    void getCategoryBreakdown_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/category-breakdown")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(3)
    void getRecentTransactions_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/recent")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(4)
    void getMonthlySummary_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/monthly-summary")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(5)
    void getDashboard_unauthenticated_returns401Or403() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/summary"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Order(6)
    void getDashboard_asViewer_returns200() throws Exception {
        // Register a viewer
        var registerRequest = new com.fintech.dashboard.dto.request.RegisterRequest(
                "Dashboard Viewer", "dashviewer@test.com", "password123"
        );
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        String viewerToken = loginAndGetToken("dashviewer@test.com", "password123");

        // Viewer SHOULD be able to view dashboard data
        mockMvc.perform(get("/api/v1/dashboard/summary")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}

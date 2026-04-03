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
 * Integration tests for Financial Record endpoints.
 * Tests CRUD operations and access control.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FinancialRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String adminToken;
    private static String viewerToken;

    /**
     * Helper to login and extract JWT token.
     */
    private String loginAndGetToken(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, password);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("data").get("token").asText();
    }

    @BeforeEach
    void setUp() throws Exception {
        if (adminToken == null) {
            adminToken = loginAndGetToken("admin@fintech.com", "admin123");
        }
    }

    // ─── Create Record Tests ───

    @Test
    @Order(1)
    void createRecord_asAdmin_returns201() throws Exception {
        FinancialRecordRequest request = new FinancialRecordRequest(
                new BigDecimal("5000.00"), RecordType.INCOME, "Salary",
                LocalDate.of(2024, 6, 15), "Monthly salary"
        );

        mockMvc.perform(post("/api/v1/records")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.amount").value(5000.00))
                .andExpect(jsonPath("$.data.type").value("INCOME"))
                .andExpect(jsonPath("$.data.category").value("Salary"));
    }

    @Test
    @Order(2)
    void createRecord_withInvalidAmount_returns400() throws Exception {
        FinancialRecordRequest request = new FinancialRecordRequest(
                new BigDecimal("-100"), RecordType.EXPENSE, "Food",
                LocalDate.of(2024, 6, 15), "Lunch"
        );

        mockMvc.perform(post("/api/v1/records")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    void createRecord_withMissingCategory_returns400() throws Exception {
        FinancialRecordRequest request = new FinancialRecordRequest(
                new BigDecimal("100"), RecordType.EXPENSE, "",
                LocalDate.of(2024, 6, 15), null
        );

        mockMvc.perform(post("/api/v1/records")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ─── Read Record Tests ───

    @Test
    @Order(4)
    void getRecords_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/records")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @Order(5)
    void getRecords_withFilters_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/records")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("type", "INCOME")
                        .param("category", "Salary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @Order(6)
    void getRecords_unauthenticated_returns401Or403() throws Exception {
        mockMvc.perform(get("/api/v1/records"))
                .andExpect(status().is4xxClientError());
    }

    // ─── Access Control Tests ───

    @Test
    @Order(7)
    void createRecord_asViewer_returns403() throws Exception {
        // First register a viewer
        var registerRequest = new com.fintech.dashboard.dto.request.RegisterRequest(
                "Viewer User", "viewer@test.com", "password123"
        );
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        viewerToken = loginAndGetToken("viewer@test.com", "password123");

        FinancialRecordRequest request = new FinancialRecordRequest(
                new BigDecimal("100"), RecordType.EXPENSE, "Food",
                LocalDate.of(2024, 6, 15), null
        );

        // Viewer should NOT be able to create records
        mockMvc.perform(post("/api/v1/records")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(8)
    void viewRecords_asViewer_returns200() throws Exception {
        if (viewerToken == null) {
            viewerToken = loginAndGetToken("viewer@test.com", "password123");
        }

        // Viewer SHOULD be able to read records
        mockMvc.perform(get("/api/v1/records")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @Order(9)
    void deleteRecord_asViewer_returns403() throws Exception {
        if (viewerToken == null) {
            viewerToken = loginAndGetToken("viewer@test.com", "password123");
        }

        // Viewer should NOT be able to delete records
        mockMvc.perform(delete("/api/v1/records/1")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isForbidden());
    }
}

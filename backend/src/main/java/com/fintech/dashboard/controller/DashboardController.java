package com.fintech.dashboard.controller;

import com.fintech.dashboard.dto.response.*;
import com.fintech.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Dashboard analytics endpoints — ANALYST and ADMIN only.
 *
 * These endpoints aggregate data from financial records.
 * All heavy computation is pushed to the database layer.
 *
 * WHY separate /summary endpoint that combines everything?
 * - Reduces HTTP round trips: frontend dashboard can fetch ALL data in one call
 * - Individual endpoints (/category-breakdown, /recent) exist for:
 *   1. Partial refreshes (e.g., auto-refresh recent transactions every 30s)
 *   2. Components that only need specific data
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
@Tag(name = "Dashboard", description = "Analytics and summary endpoints (All authenticated users)")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "Full dashboard summary",
            description = "Returns total income, expenses, net balance, category breakdown, and recent transactions")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary() {
        DashboardSummaryResponse summary = dashboardService.getSummary();
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/category-breakdown")
    @Operation(summary = "Category-wise breakdown", description = "Income and expenses grouped by category")
    public ResponseEntity<ApiResponse<List<CategoryBreakdownResponse>>> getCategoryBreakdown() {
        List<CategoryBreakdownResponse> breakdown = dashboardService.getCategoryBreakdown();
        return ResponseEntity.ok(ApiResponse.success(breakdown));
    }

    @GetMapping("/recent")
    @Operation(summary = "Recent transactions", description = "Last 5 transactions")
    public ResponseEntity<ApiResponse<List<FinancialRecordResponse>>> getRecentTransactions() {
        List<FinancialRecordResponse> recent = dashboardService.getRecentTransactions();
        return ResponseEntity.ok(ApiResponse.success(recent));
    }

    @GetMapping("/monthly-summary")
    @Operation(summary = "Monthly summary", description = "Month-by-month income vs expense breakdown")
    public ResponseEntity<ApiResponse<List<MonthlySummaryResponse>>> getMonthlySummary() {
        List<MonthlySummaryResponse> monthly = dashboardService.getMonthlySummary();
        return ResponseEntity.ok(ApiResponse.success(monthly));
    }
}

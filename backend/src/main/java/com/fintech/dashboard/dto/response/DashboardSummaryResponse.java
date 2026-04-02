package com.fintech.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Top-level dashboard summary combining all analytics in one response.
 * Single API call returns everything the frontend dashboard needs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryResponse {

    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;     // totalIncome - totalExpenses
    private List<CategoryBreakdownResponse> categoryBreakdown;
    private List<FinancialRecordResponse> recentTransactions;
}

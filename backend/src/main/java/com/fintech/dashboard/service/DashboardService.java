package com.fintech.dashboard.service;

import com.fintech.dashboard.dto.response.CategoryBreakdownResponse;
import com.fintech.dashboard.dto.response.DashboardSummaryResponse;
import com.fintech.dashboard.dto.response.FinancialRecordResponse;
import com.fintech.dashboard.dto.response.MonthlySummaryResponse;
import com.fintech.dashboard.model.enums.RecordType;
import com.fintech.dashboard.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dashboard analytics service.
 *
 * All aggregation happens in the DATABASE (via JPQL queries) — not in Java.
 * WHY? Databases are optimized for aggregation. Pulling millions of records
 * into memory to sum them in Java would be catastrophically slow and wasteful.
 *
 * The monthly summary is the only query that needs post-processing in Java
 * because we need to merge INCOME and EXPENSE rows for the same month into
 * a single MonthlySummaryResponse.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FinancialRecordRepository recordRepository;

    /**
     * Full dashboard summary in one call.
     * Combines income, expenses, net balance, category breakdown, and recent transactions.
     */
    public DashboardSummaryResponse getSummary() {
        BigDecimal totalIncome = recordRepository.getTotalIncome();
        BigDecimal totalExpenses = recordRepository.getTotalExpenses();

        return DashboardSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(totalIncome.subtract(totalExpenses))
                .categoryBreakdown(getCategoryBreakdown())
                .recentTransactions(getRecentTransactions())
                .build();
    }

    public List<CategoryBreakdownResponse> getCategoryBreakdown() {
        return recordRepository.getCategoryBreakdown().stream()
                .map(row -> CategoryBreakdownResponse.builder()
                        .type((RecordType) row[0])
                        .category((String) row[1])
                        .total((BigDecimal) row[2])
                        .build())
                .collect(Collectors.toList());
    }

    public List<FinancialRecordResponse> getRecentTransactions() {
        return recordRepository.findRecentTransactions(PageRequest.of(0, 5))
                .stream()
                .map(FinancialRecordResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Monthly summary: merge income/expense rows for the same (year, month).
     *
     * The database query returns rows like:
     *   [2024, 3, INCOME, 5000.00]
     *   [2024, 3, EXPENSE, 2000.00]
     *
     * We merge these into:
     *   { year: 2024, month: 3, totalIncome: 5000, totalExpenses: 2000, netBalance: 3000 }
     */
    public List<MonthlySummaryResponse> getMonthlySummary() {
        List<Object[]> rows = recordRepository.getMonthlySummary();
        Map<String, MonthlySummaryResponse> monthlyMap = new LinkedHashMap<>();

        for (Object[] row : rows) {
            int year = (int) row[0];
            int month = (int) row[1];
            RecordType type = (RecordType) row[2];
            BigDecimal total = (BigDecimal) row[3];

            String key = year + "-" + month;
            MonthlySummaryResponse summary = monthlyMap.computeIfAbsent(key, k ->
                    MonthlySummaryResponse.builder()
                            .year(year)
                            .month(month)
                            .totalIncome(BigDecimal.ZERO)
                            .totalExpenses(BigDecimal.ZERO)
                            .netBalance(BigDecimal.ZERO)
                            .build()
            );

            if (type == RecordType.INCOME) {
                summary.setTotalIncome(total);
            } else {
                summary.setTotalExpenses(total);
            }
            summary.setNetBalance(summary.getTotalIncome().subtract(summary.getTotalExpenses()));
        }

        return new ArrayList<>(monthlyMap.values());
    }
}

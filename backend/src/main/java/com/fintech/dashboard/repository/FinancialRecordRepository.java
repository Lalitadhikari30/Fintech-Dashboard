package com.fintech.dashboard.repository;

import com.fintech.dashboard.model.FinancialRecord;
import com.fintech.dashboard.model.enums.RecordType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Financial record data access layer.
 *
 * All queries filter out soft-deleted records (deleted = false).
 * JPQL is used for complex queries — it's database-agnostic and readable.
 *
 * WHY custom @Query instead of derived method names?
 * - Derived names get unreadable fast:
 *   findByDeletedFalseAndTypeAndCreatedByIdAndRecordDateBetween(...) — no thanks.
 * - JPQL gives us explicit control over the query and makes the intent obvious.
 */
@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {

    /** Find a non-deleted record by ID */
    Optional<FinancialRecord> findByIdAndDeletedFalse(Long id);

    /**
     * Flexible filter query.
     * Each parameter is optional — if null, that condition is skipped.
     * This single query handles all filter combinations without building dynamic queries.
     */
    @Query("SELECT r FROM FinancialRecord r WHERE r.deleted = false " +
           "AND (:type IS NULL OR r.type = :type) " +
           "AND (:category IS NULL OR r.category = :category) " +
           "AND (:userId IS NULL OR r.createdBy.id = :userId) " +
           "AND (:startDate IS NULL OR r.recordDate >= :startDate) " +
           "AND (:endDate IS NULL OR r.recordDate <= :endDate)")
    Page<FinancialRecord> findWithFilters(
            @Param("type") RecordType type,
            @Param("category") String category,
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    // ─── Dashboard Aggregation Queries ───

    /** Sum all income (non-deleted) */
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r " +
           "WHERE r.deleted = false AND r.type = 'INCOME'")
    BigDecimal getTotalIncome();

    /** Sum all expenses (non-deleted) */
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r " +
           "WHERE r.deleted = false AND r.type = 'EXPENSE'")
    BigDecimal getTotalExpenses();

    /** Category-wise totals grouped by type and category */
    @Query("SELECT r.type, r.category, SUM(r.amount) FROM FinancialRecord r " +
           "WHERE r.deleted = false GROUP BY r.type, r.category ORDER BY r.type, r.category")
    List<Object[]> getCategoryBreakdown();

    /** Last N transactions ordered by date descending, then by creation time */
    @Query("SELECT r FROM FinancialRecord r WHERE r.deleted = false " +
           "ORDER BY r.recordDate DESC, r.createdAt DESC")
    List<FinancialRecord> findRecentTransactions(Pageable pageable);

    /**
     * Monthly summary: year, month, type, total
     * Uses YEAR() and MONTH() functions — supported by MySQL and most JPA providers.
     */
    @Query("SELECT YEAR(r.recordDate), MONTH(r.recordDate), r.type, SUM(r.amount) " +
           "FROM FinancialRecord r WHERE r.deleted = false " +
           "GROUP BY YEAR(r.recordDate), MONTH(r.recordDate), r.type " +
           "ORDER BY YEAR(r.recordDate) DESC, MONTH(r.recordDate) DESC")
    List<Object[]> getMonthlySummary();
}

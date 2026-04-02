package com.fintech.dashboard.model;

import com.fintech.dashboard.model.enums.RecordType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A single financial transaction (income or expense).
 *
 * Design notes:
 * - BigDecimal for amount: avoids floating-point errors (critical in fintech)
 * - DECIMAL(19,4) provides enough precision for currencies including crypto
 * - Soft delete via 'deleted' flag: financial records should never be permanently erased
 * - record_date is LocalDate (not LocalDateTime): a transaction belongs to a business day
 * - Composite index on (created_by, type, record_date) covers the most common query pattern:
 *   "show me this user's income records between these dates"
 */
@Entity
@Table(name = "financial_records", indexes = {
        @Index(name = "idx_record_type", columnList = "type"),
        @Index(name = "idx_record_category", columnList = "category"),
        @Index(name = "idx_record_date", columnList = "record_date"),
        @Index(name = "idx_record_created_by", columnList = "created_by"),
        @Index(name = "idx_record_composite", columnList = "created_by, type, record_date"), // covers common filter queries
        @Index(name = "idx_record_deleted", columnList = "deleted")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecordType type;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(length = 500)
    private String description;

    // Soft delete: true = logically removed, excluded from queries by default
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @ManyToOne(fetch = FetchType.LAZY) // LAZY: don't load the full User object unless needed
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

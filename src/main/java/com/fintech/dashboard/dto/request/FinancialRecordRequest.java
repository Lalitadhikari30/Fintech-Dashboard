package com.fintech.dashboard.dto.request;

import com.fintech.dashboard.model.enums.RecordType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload for creating / updating a financial record.
 *
 * Validation rules:
 * - amount must be positive (> 0) — zero-value transactions are meaningless
 * - type must be a valid RecordType enum
 * - category is required and capped at 100 chars
 * - recordDate is required — we don't assume "today" because backdated entries are common
 * - description is optional, capped at 500 chars
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialRecordRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Type is required (INCOME or EXPENSE)")
    private RecordType type;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @NotNull(message = "Record date is required")
    private LocalDate recordDate;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}

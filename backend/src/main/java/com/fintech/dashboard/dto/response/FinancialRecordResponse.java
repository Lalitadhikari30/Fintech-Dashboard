package com.fintech.dashboard.dto.response;

import com.fintech.dashboard.model.FinancialRecord;
import com.fintech.dashboard.model.enums.RecordType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialRecordResponse {

    private Long id;
    private BigDecimal amount;
    private RecordType type;
    private String category;
    private LocalDate recordDate;
    private String description;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;

    public static FinancialRecordResponse fromEntity(FinancialRecord record) {
        return FinancialRecordResponse.builder()
                .id(record.getId())
                .amount(record.getAmount())
                .type(record.getType())
                .category(record.getCategory())
                .recordDate(record.getRecordDate())
                .description(record.getDescription())
                .createdById(record.getCreatedBy().getId())
                .createdByName(record.getCreatedBy().getName())
                .createdAt(record.getCreatedAt())
                .build();
    }
}

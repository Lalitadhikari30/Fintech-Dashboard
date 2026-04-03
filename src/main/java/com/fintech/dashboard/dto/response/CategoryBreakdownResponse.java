package com.fintech.dashboard.dto.response;

import com.fintech.dashboard.model.enums.RecordType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryBreakdownResponse {

    private RecordType type;       // INCOME or EXPENSE
    private String category;       // e.g., "Salary", "Rent", "Food"
    private BigDecimal total;      // sum of amounts for this type+category
}

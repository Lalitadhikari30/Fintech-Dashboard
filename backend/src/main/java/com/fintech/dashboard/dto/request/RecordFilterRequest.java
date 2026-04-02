package com.fintech.dashboard.dto.request;

import com.fintech.dashboard.model.enums.RecordType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Query parameters for filtering financial records.
 * All fields are optional — null means "don't filter on this field."
 * Spring Boot binds query params to this object automatically via @ModelAttribute.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordFilterRequest {

    private RecordType type;

    private String category;

    private Long userId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) // expects yyyy-MM-dd
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
}

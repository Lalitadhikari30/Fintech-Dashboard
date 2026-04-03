package com.fintech.dashboard.controller;

import com.fintech.dashboard.dto.request.FinancialRecordRequest;
import com.fintech.dashboard.dto.request.RecordFilterRequest;
import com.fintech.dashboard.dto.response.ApiResponse;
import com.fintech.dashboard.dto.response.FinancialRecordResponse;
import com.fintech.dashboard.service.FinancialRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Financial record CRUD endpoints.
 *
 * Access control:
 * - GET (read): any authenticated user (VIEWER, ANALYST, ADMIN)
 * - POST/PUT/DELETE (write): ADMIN only
 *
 * The @PreAuthorize annotations are METHOD-level here (not class-level)
 * because different HTTP methods have different access requirements.
 */
@RestController
@RequestMapping("/api/v1/records")
@RequiredArgsConstructor
@Tag(name = "Financial Records", description = "CRUD operations for financial records")
public class FinancialRecordController {

    private final FinancialRecordService recordService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a financial record", description = "ADMIN only")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> createRecord(
            @Valid @RequestBody FinancialRecordRequest request) {
        FinancialRecordResponse record = recordService.createRecord(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Record created successfully", record));
    }

    /**
     * Filter records with optional query parameters.
     * Spring Boot automatically binds query params to RecordFilterRequest fields.
     *
     * Example: GET /api/v1/records?type=INCOME&category=Salary&startDate=2024-01-01&page=0&size=10
     */
    @GetMapping
    @Operation(summary = "List records with filters", description = "Filter by type, category, date range, user. Paginated.")
    public ResponseEntity<ApiResponse<Page<FinancialRecordResponse>>> getRecords(
            @ModelAttribute RecordFilterRequest filter,
            @PageableDefault(size = 10, sort = "recordDate") Pageable pageable) {
        Page<FinancialRecordResponse> records = recordService.getRecords(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get record by ID")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> getRecordById(
            @PathVariable Long id) {
        FinancialRecordResponse record = recordService.getRecordById(id);
        return ResponseEntity.ok(ApiResponse.success(record));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a financial record", description = "ADMIN only")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody FinancialRecordRequest request) {
        FinancialRecordResponse record = recordService.updateRecord(id, request);
        return ResponseEntity.ok(ApiResponse.success("Record updated successfully", record));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a financial record", description = "ADMIN only. Record is marked as deleted, not removed.")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(@PathVariable Long id) {
        recordService.deleteRecord(id);
        return ResponseEntity.ok(ApiResponse.success("Record deleted successfully", null));
    }
}

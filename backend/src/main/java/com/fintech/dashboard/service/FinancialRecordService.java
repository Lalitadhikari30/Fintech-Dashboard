package com.fintech.dashboard.service;

import com.fintech.dashboard.dto.request.FinancialRecordRequest;
import com.fintech.dashboard.dto.request.RecordFilterRequest;
import com.fintech.dashboard.dto.response.FinancialRecordResponse;
import com.fintech.dashboard.exception.ResourceNotFoundException;
import com.fintech.dashboard.model.FinancialRecord;
import com.fintech.dashboard.model.User;
import com.fintech.dashboard.repository.FinancialRecordRepository;
import com.fintech.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Financial record CRUD service.
 *
 * Key behaviors:
 * - Create: links record to the currently authenticated user (from JWT)
 * - Read: supports flexible filtering via the single JPQL query in the repository
 * - Update: only non-deleted records can be updated
 * - Delete: SOFT delete only — sets deleted=true, never removes from DB
 *
 * WHY soft delete? In fintech, audit trails are mandatory. You need to prove
 * what was recorded and when. Hard deletes destroy evidence.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialRecordService {

    private final FinancialRecordRepository recordRepository;
    private final UserRepository userRepository;

    @Transactional
    public FinancialRecordResponse createRecord(FinancialRecordRequest request) {
        User currentUser = getCurrentUser();

        FinancialRecord record = FinancialRecord.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .recordDate(request.getRecordDate())
                .description(request.getDescription())
                .createdBy(currentUser)
                .deleted(false)
                .build();

        FinancialRecord saved = recordRepository.save(record);
        log.info("Created {} record: {} {} by user {}",
                saved.getType(), saved.getAmount(), saved.getCategory(),
                currentUser.getEmail());
        return FinancialRecordResponse.fromEntity(saved);
    }

    public Page<FinancialRecordResponse> getRecords(RecordFilterRequest filter, Pageable pageable) {
        return recordRepository.findWithFilters(
                filter.getType(),
                filter.getCategory(),
                filter.getUserId(),
                filter.getStartDate(),
                filter.getEndDate(),
                pageable
        ).map(FinancialRecordResponse::fromEntity);
    }

    public FinancialRecordResponse getRecordById(Long id) {
        FinancialRecord record = findRecordOrThrow(id);
        return FinancialRecordResponse.fromEntity(record);
    }

    @Transactional
    public FinancialRecordResponse updateRecord(Long id, FinancialRecordRequest request) {
        FinancialRecord record = findRecordOrThrow(id);

        record.setAmount(request.getAmount());
        record.setType(request.getType());
        record.setCategory(request.getCategory());
        record.setRecordDate(request.getRecordDate());
        record.setDescription(request.getDescription());

        FinancialRecord updated = recordRepository.save(record);
        log.info("Updated record {}: {} {} {}", id,
                updated.getType(), updated.getAmount(), updated.getCategory());
        return FinancialRecordResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteRecord(Long id) {
        FinancialRecord record = findRecordOrThrow(id);
        record.setDeleted(true); // SOFT delete
        recordRepository.save(record);
        log.info("Soft-deleted record {}", id);
    }

    // ─── Helpers ───

    private FinancialRecord findRecordOrThrow(Long id) {
        return recordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial Record", "id", id));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}

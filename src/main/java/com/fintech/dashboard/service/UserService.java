package com.fintech.dashboard.service;

import com.fintech.dashboard.dto.request.UpdateRoleRequest;
import com.fintech.dashboard.dto.request.UpdateStatusRequest;
import com.fintech.dashboard.dto.response.UserResponse;
import com.fintech.dashboard.exception.ResourceNotFoundException;
import com.fintech.dashboard.model.User;
import com.fintech.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User management service — ADMIN-only operations.
 *
 * Note: Access control is enforced at the CONTROLLER level via @PreAuthorize.
 * The service layer focuses on business logic, not authorization checks.
 * This separation keeps services reusable (e.g., internal system calls don't need auth).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserResponse::fromEntity); // Page.map preserves pagination metadata
    }

    public UserResponse getUserById(Long id) {
        User user = findUserOrThrow(id);
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse updateRole(Long id, UpdateRoleRequest request) {
        User user = findUserOrThrow(id);
        log.info("Updating role for user {} from {} to {}",
                user.getEmail(), user.getRole(), request.getRole());
        user.setRole(request.getRole());
        return UserResponse.fromEntity(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateStatus(Long id, UpdateStatusRequest request) {
        User user = findUserOrThrow(id);
        log.info("Updating status for user {} from {} to {}",
                user.getEmail(), user.getStatus(), request.getStatus());
        user.setStatus(request.getStatus());
        return UserResponse.fromEntity(userRepository.save(user));
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
}

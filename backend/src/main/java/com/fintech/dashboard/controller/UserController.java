package com.fintech.dashboard.controller;

import com.fintech.dashboard.dto.request.UpdateRoleRequest;
import com.fintech.dashboard.dto.request.UpdateStatusRequest;
import com.fintech.dashboard.dto.response.ApiResponse;
import com.fintech.dashboard.dto.response.UserResponse;
import com.fintech.dashboard.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * User management endpoints — ADMIN-only.
 *
 * @PreAuthorize is the ACTUAL enforcement mechanism (not just documentation).
 * Spring Security evaluates the SpEL expression BEFORE the method executes.
 * If the current user doesn't have ROLE_ADMIN, they get 403 Forbidden.
 *
 * WHY PATCH instead of PUT for role/status updates?
 * - PUT implies replacing the ENTIRE resource
 * - PATCH implies modifying a PART of the resource
 * - Updating just the role is a partial update → PATCH is semantically correct
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // class-level: ALL endpoints in this controller require ADMIN
@Tag(name = "User Management", description = "ADMIN-only user management operations")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "List all users", description = "Paginated list of all users (ADMIN only)")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PatchMapping("/{id}/role")
    @Operation(summary = "Update user role", description = "Change a user's role (ADMIN only)")
    public ResponseEntity<ApiResponse<UserResponse>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        UserResponse user = userService.updateRole(id, request);
        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", user));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update user status", description = "Activate or deactivate a user (ADMIN only)")
    public ResponseEntity<ApiResponse<UserResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        UserResponse user = userService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", user));
    }
}

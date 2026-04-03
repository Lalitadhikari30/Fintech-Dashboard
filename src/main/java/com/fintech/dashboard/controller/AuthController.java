package com.fintech.dashboard.controller;

import com.fintech.dashboard.dto.request.LoginRequest;
import com.fintech.dashboard.dto.request.RegisterRequest;
import com.fintech.dashboard.dto.response.ApiResponse;
import com.fintech.dashboard.dto.response.AuthResponse;
import com.fintech.dashboard.dto.response.UserResponse;
import com.fintech.dashboard.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration and login")
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user.
     *
     * Returns 201 CREATED (not 200 OK) because a new resource was created.
     * This is a REST best practice — status codes should be semantically correct.
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user with VIEWER role")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        UserResponse user = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", user));
    }

    /**
     * Login and receive JWT token.
     *
     * Returns 200 OK with the token in the response body.
     * Client should store this token and include it in the Authorization header
     * for all subsequent requests.
     */
    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate and receive JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse auth = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", auth));
    }
}

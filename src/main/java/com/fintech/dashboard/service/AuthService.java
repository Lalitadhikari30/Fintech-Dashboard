package com.fintech.dashboard.service;

import com.fintech.dashboard.dto.request.LoginRequest;
import com.fintech.dashboard.dto.request.RegisterRequest;
import com.fintech.dashboard.dto.response.AuthResponse;
import com.fintech.dashboard.dto.response.UserResponse;
import com.fintech.dashboard.exception.DuplicateResourceException;
import com.fintech.dashboard.exception.UnauthorizedException;
import com.fintech.dashboard.model.User;
import com.fintech.dashboard.model.enums.Role;
import com.fintech.dashboard.model.enums.UserStatus;
import com.fintech.dashboard.repository.UserRepository;
import com.fintech.dashboard.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication service — registration and login.
 *
 * Registration flow:
 * 1. Check email uniqueness
 * 2. Hash password with BCrypt
 * 3. Save user with VIEWER role (least privilege)
 * 4. Return user info (no auto-login — client must call /login)
 *
 * Login flow:
 * 1. Spring Security AuthenticationManager validates credentials
 * 2. If valid, generate JWT
 * 3. Return token + user info
 *
 * WHY separate register and login?
 * - Registration might need email verification in production
 * - Keeps the flows independent and testable
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        // Fail fast if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.VIEWER)          // default: least privilege
                .status(UserStatus.ACTIVE)  // active by default
                .build();

        User saved = userRepository.save(user);
        log.info("New user registered: {} ({})", saved.getEmail(), saved.getRole());
        return UserResponse.fromEntity(saved);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            String token = jwtTokenProvider.generateToken(authentication);
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UnauthorizedException("User not found"));

            log.info("User logged in: {} ({})", user.getEmail(), user.getRole());
            return AuthResponse.of(token, user.getEmail(), user.getRole().name());

        } catch (DisabledException ex) {
            throw new UnauthorizedException("Account is deactivated. Contact an administrator.");
        } catch (BadCredentialsException ex) {
            throw new UnauthorizedException("Invalid email or password");
        }
    }
}

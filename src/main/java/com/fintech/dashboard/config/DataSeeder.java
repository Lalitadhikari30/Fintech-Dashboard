package com.fintech.dashboard.config;

import com.fintech.dashboard.model.User;
import com.fintech.dashboard.model.enums.Role;
import com.fintech.dashboard.model.enums.UserStatus;
import com.fintech.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a default ADMIN user on first startup.
 *
 * WHY CommandLineRunner instead of data.sql?
 * - We need to BCrypt-hash the password, which can't be done in plain SQL
 * - It only creates the admin if it doesn't already exist (idempotent)
 *
 * Default admin credentials:
 *   Email: admin@fintech.com
 *   Password: admin123
 *
 * ⚠️ Change these in production!
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminEmail = "admin@fintech.com";

        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .name("System Admin")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();

            userRepository.save(admin);
            log.info("══════════════════════════════════════════");
            log.info("  Default ADMIN user created");
            log.info("  Email:    {}", adminEmail);
            log.info("  Password: admin123");
            log.info("  ⚠️  Change these credentials in production!");
            log.info("══════════════════════════════════════════");
        } else {
            log.info("Admin user already exists, skipping seed.");
        }
    }
}

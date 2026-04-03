package com.fintech.dashboard.model;

import com.fintech.dashboard.model.enums.Role;
import com.fintech.dashboard.model.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Core user entity.
 *
 * Design notes:
 * - email is UNIQUE — used as the login identifier (not username)
 * - password is stored as a BCrypt hash (never plain text)
 * - @EntityListeners enables automatic createdAt/updatedAt population
 * - @ToString.Exclude on password prevents accidental logging of credentials
 * - The relationship to FinancialRecord is mapped but lazy-loaded to avoid N+1 issues
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_role", columnList = "role"),
        @Index(name = "idx_user_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL auto-increment
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @ToString.Exclude // never log passwords
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.VIEWER; // safest default — least privilege principle

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // One user → many financial records
    // CascadeType.ALL: if we delete a user, their records go too (soft-delete handles this gracefully)
    // orphanRemoval: records detached from the user collection get deleted
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FinancialRecord> financialRecords = new ArrayList<>();
}

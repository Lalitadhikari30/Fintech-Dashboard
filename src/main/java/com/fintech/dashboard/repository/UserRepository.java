package com.fintech.dashboard.repository;

import com.fintech.dashboard.model.User;
import com.fintech.dashboard.model.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User data access layer.
 *
 * Spring Data JPA generates the implementation at runtime from method names.
 * We only declare the custom finders we need — standard CRUD comes free from JpaRepository.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Used during login — email is the unique login identifier */
    Optional<User> findByEmail(String email);

    /** Pre-registration check to prevent duplicate accounts */
    boolean existsByEmail(String email);

    /** Find by email AND status — used to block inactive users during auth */
    Optional<User> findByEmailAndStatus(String email, UserStatus status);
}

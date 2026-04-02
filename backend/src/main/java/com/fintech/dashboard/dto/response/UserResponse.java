package com.fintech.dashboard.dto.response;

import com.fintech.dashboard.model.User;
import com.fintech.dashboard.model.enums.Role;
import com.fintech.dashboard.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User response DTO.
 * Notice: NO password field. DTOs are the firewall between your database and the client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private UserStatus status;
    private LocalDateTime createdAt;

    /** Manual mapper — keeps it simple, no MapStruct dependency needed */
    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

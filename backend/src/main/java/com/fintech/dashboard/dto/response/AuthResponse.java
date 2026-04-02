package com.fintech.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String type;     // "Bearer"
    private String email;
    private String role;

    public static AuthResponse of(String token, String email, String role) {
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .email(email)
                .role(role)
                .build();
    }
}

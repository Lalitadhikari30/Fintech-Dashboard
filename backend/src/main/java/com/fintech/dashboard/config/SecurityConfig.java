package com.fintech.dashboard.config;

import com.fintech.dashboard.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Central Spring Security configuration.
 *
 * Design decisions:
 * - STATELESS sessions: JWT is self-contained, no server-side session needed
 * - CSRF disabled: Not needed for stateless REST APIs (no cookies = no CSRF risk)
 * - CORS enabled: Allows frontend on a different port/domain to call the API
 * - @EnableMethodSecurity: Enables @PreAuthorize annotations on controller methods
 *   (this is where RBAC is actually enforced)
 * - URL-level rules are a FALLBACK; method-level @PreAuthorize is the primary guard
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // enables @PreAuthorize, @Secured, etc.
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .headers(headers -> headers.frameOptions(frame -> frame.disable())) // H2 console uses iframes
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/v1/auth/**").permitAll()
                // Swagger UI
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                                 "/api-docs/**", "/v3/api-docs/**").permitAll()
                // H2 Console (dev only)
                .requestMatchers("/h2-console/**").permitAll()
                // User management — ADMIN only
                .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
                // Dashboard — ANALYST and ADMIN
                .requestMatchers("/api/v1/dashboard/**").hasAnyRole("ANALYST", "ADMIN")
                // Records: read is open to all authenticated, write is ADMIN only
                .requestMatchers(HttpMethod.GET, "/api/v1/records/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/records/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/records/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/records/**").hasRole("ADMIN")
                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            // Insert our JWT filter BEFORE Spring's default username/password filter
            .addFilterBefore(jwtAuthenticationFilter,
                             UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // industry standard, adaptive hashing
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173")); // common frontend ports
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

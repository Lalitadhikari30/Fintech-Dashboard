package com.fintech.dashboard.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT utility class — handles token creation, parsing, and validation.
 *
 * Token structure:
 * - Subject: user email (unique identifier)
 * - Claim "role": user role (ADMIN/ANALYST/VIEWER)
 * - Expiration: configurable via application.yml (default 24h)
 *
 * WHY HMAC-SHA256?
 * - Simple, fast, and sufficient for a monolithic app where the same server
 *   both signs and verifies tokens.
 * - For microservices, you'd use RSA (asymmetric) so services can verify
 *   without knowing the signing key.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMs = expirationMs;
    }

    /** Generate a JWT for an authenticated user */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Extract role from authorities (we store it as "ROLE_ADMIN", etc.)
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("VIEWER");

        return Jwts.builder()
                .subject(userDetails.getUsername()) // email
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    /** Extract email (subject) from token */
    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /** Validate token — checks signature, expiration, and format */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("JWT expired: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("Invalid JWT: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.warn("Unsupported JWT: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

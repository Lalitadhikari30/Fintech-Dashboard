package com.fintech.dashboard.security;

import com.fintech.dashboard.model.User;
import com.fintech.dashboard.model.enums.UserStatus;
import com.fintech.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Bridges our User entity with Spring Security's UserDetails interface.
 *
 * Spring Security calls loadUserByUsername() during authentication.
 * We load by EMAIL (our unique login identifier), not username.
 *
 * Key behaviors:
 * - Inactive users are treated as "disabled" — Spring Security will reject them
 * - Role is mapped to a GrantedAuthority with ROLE_ prefix (Spring Security convention)
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getStatus() == UserStatus.ACTIVE,  // enabled
                true,                                     // accountNonExpired
                true,                                     // credentialsNonExpired
                true,                                     // accountNonLocked
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                )
        );
    }
}

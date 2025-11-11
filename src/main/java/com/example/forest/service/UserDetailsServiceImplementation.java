package com.example.forest.service;

import com.example.forest.document.MongoUserDocument;
import com.example.forest.repository.mongodb.MongoUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

import static java.util.Collections.singletonList;

/**
 * Custom implementation of Spring Security's {@link UserDetailsService} for loading
 * user-specific authentication data from MongoDB.
 *
 * <p>Used by Spring Security during authentication to verify user credentials and assign authorities.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImplementation implements UserDetailsService {

    private final MongoUserRepository userRepository;

    /**
     * Loads a user by their username for authentication.
     *
     * @param username the username identifying the user
     * @return the {@link UserDetails} object containing user credentials and authorities
     * @throws UsernameNotFoundException if no user with the given username exists
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isBlank()) {
            throw new UsernameNotFoundException("Username cannot be null or empty.");
        }

        log.debug("Attempting to load user with username: {}", username);

        Optional<MongoUserDocument> userOptional = userRepository.findByUsername(username);
        MongoUserDocument user = userOptional.orElseThrow(() -> {
            log.warn("User '{}' not found in database.", username);
            return new UsernameNotFoundException("User '" + username + "' could not be found.");
        });

        log.info("✅ User '{}' loaded successfully. Enabled: {}", user.getUsername(), user.isEnabled());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true,   // accountNonExpired
                true,   // credentialsNonExpired
                true,   // accountNonLocked
                getAuthorities(user.getRole().name())
        );
    }

    /**
     * Maps the user's role to Spring Security's {@link GrantedAuthority}.
     *
     * @param role the user's role (e.g., ADMIN, USER)
     * @return a collection containing a single {@link SimpleGrantedAuthority}
     */
    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
        return singletonList(new SimpleGrantedAuthority(role));
    }
}

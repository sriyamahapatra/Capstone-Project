package com.example.forest.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SecurityConfig.java
 *
 * Configures global application security using Spring Security.
 * Handles JWT-based authentication, defines public routes, and manages CORS.
 */
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // Custom JWT authentication filter that validates tokens in requests
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Defines Spring Security's filter chain configuration.
     * Controls access to endpoints, CORS, sessions, and exception handling.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Disable CSRF as JWT authentication is stateless
                .csrf(AbstractHttpConfigurer::disable)

                // Enable CORS using configuration defined below
                .cors(Customizer.withDefaults())

                // Disable sessions — authentication handled entirely via JWT
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization configuration for endpoints
                .authorizeHttpRequests(auth -> auth

                        // Public authentication endpoints (login, register, etc.)
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/favicon.ico"
                        ).permitAll()

                        // Allow public GET requests for read-only data
                        .requestMatchers(HttpMethod.GET, "/api/v1/subreddit/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/mongo/posts/").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/mongo/posts/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/comments/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/trending").permitAll()
                        .requestMatchers("/api/v1/subscriptions/**").permitAll()

                        // Allow media APIs (photo/video upload & fetch) publicly
                        .requestMatchers(HttpMethod.POST, "/api/v1/photos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/photos/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/videos/add").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/videos/**").permitAll()

                        // Public access for API documentation
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-resources/**"
                        ).permitAll()

                        // Public endpoint for RAG (AI chat)
                        .requestMatchers("/api/chat/**").permitAll()

                        // Any other request must be authenticated
                        .anyRequest().authenticated()
                )

                // Enable JWT-based OAuth2 resource server
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))

                // Configure handling for authentication and access errors
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint()) // Handles 401 errors
                        .accessDeniedHandler(new BearerTokenAccessDeniedHandler()) // Handles 403 errors
                )

                // Add custom JWT filter before Spring Security’s built-in authentication
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Finalize and build the security chain
                .build();
    }

    /**
     * Converts JWT claims (like "scope") into Spring Security roles.
     * Ensures role-based access works with JWT tokens.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<String> authorities = jwt.getClaimAsStringList("scope");
            if (authorities == null) {
                authorities = List.of(jwt.getClaimAsString("scope"));
            }
            // Map each scope to a ROLE_ authority
            return authorities.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        });
        return converter;
    }

    /**
     * Configures CORS to allow the frontend (Angular, React, etc.) to communicate with the backend.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:4200")); // Allow Angular dev server
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        cfg.setExposedHeaders(List.of("Authorization"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L); // Cache CORS preflight for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}

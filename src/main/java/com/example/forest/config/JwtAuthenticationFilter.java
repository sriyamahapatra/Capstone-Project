package com.example.forest.config;

import com.example.forest.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter.java
 *
 * Custom security filter that runs once per request.
 * It intercepts incoming HTTP requests, checks for a valid JWT token
 * in the Authorization header, and authenticates the user if valid.
 *
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Service that handles token creation, extraction, and validation
    private final JwtService jwtService;

    // Loads user details (used to verify JWT claims and authorities)
    private final UserDetailsService userDetailsService;

    /**
     * Filters each incoming request to check if a valid JWT is present.
     *
     * @param request  the HTTP request object
     * @param response the HTTP response object
     * @param filterChain allows continuing with other filters if authentication is valid
     */
    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain)
            throws ServletException, IOException {

        // Retrieve the "Authorization" header (format: "Bearer <token>")
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // If there's no token or it doesn't start with "Bearer ", skip authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT from the header by removing the "Bearer " prefix
        jwt = authHeader.substring(7);

        // Extract username (subject) from the JWT payload
        username = jwtService.extractUsername(jwt);

        // If username is valid and user is not already authenticated
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load the user from the database (UserDetails implementation)
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Validate the JWT using the user details (checks signature, expiration, etc.)
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // Create an authentication token with user details and roles
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // No password required, since JWT is already validated
                        userDetails.getAuthorities()
                );

                // Attach additional web authentication details (like IP, session info)
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Set the authentication context for the current user/session
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continue with the next filter in the chain
        filterChain.doFilter(request, response);
    }
}

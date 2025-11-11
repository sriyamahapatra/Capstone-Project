package com.example.forest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LoginRequest.java
 *
 * Data Transfer Object (DTO) representing a user's login request.
 * Captures credentials submitted by the client for authentication.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    /** The username entered by the user (required for authentication). */
    @NotBlank(message = "Username is required")
    private String username;

    /** The user's password (required and validated before authentication). */
    @NotBlank(message = "Password is required")
    private String password;
}

package com.example.forest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * RegisterRequest.java
 *
 * Data Transfer Object (DTO) used for handling user registration requests.
 * Captures the user's account details and preferences during signup.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    /** The user's email address (required and validated for proper email format). */
    @Email
    @NotEmpty(message = "Email is required")
    private String email;

    /** The username chosen by the user (required and must be unique). */
    @NotBlank(message = "Username is required")
    private String username;

    /** The user's password (required and validated before encryption). */
    @NotBlank(message = "Password is required")
    private String password;

    /**
     * Set of user-selected interests (required minimum of 3).
     * Used for personalized content recommendations and feed generation.
     */
    @Size(min = 3, message = "Please select at least 3 interests")
    private Set<String> interests;
}

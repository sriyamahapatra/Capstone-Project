package com.example.forest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RefreshTokenRequest.java
 *
 * Data Transfer Object (DTO) used when requesting a new access token
 * using an existing valid refresh token.
 * This ensures continued user authentication without requiring re-login.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRequest {

    /** The refresh token issued during login (required for generating a new access token). */
    @NotBlank
    private String refreshToken;

    /** The username associated with the refresh token. */
    private String username;
}

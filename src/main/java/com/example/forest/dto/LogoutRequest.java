package com.example.forest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LogoutRequest.java
 *
 * Data Transfer Object (DTO) representing a logout request.
 * Used when a user logs out and wants to invalidate their refresh token.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogoutRequest {

    /** The refresh token that needs to be invalidated upon logout (required). */
    @NotBlank
    private String refreshToken;
}

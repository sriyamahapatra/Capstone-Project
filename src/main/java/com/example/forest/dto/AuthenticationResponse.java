package com.example.forest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * AuthenticationResponse.java
 *
 * Data Transfer Object (DTO) representing the response sent back to the client
 * after a successful authentication or token refresh operation.
 * Contains JWT tokens and related session metadata.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthenticationResponse {

    /** The JWT access token used for authorizing API requests. */
    private String authenticationToken;

    /** The username of the authenticated user. */
    private String username;

    /** The refresh token used to obtain a new access token when it expires. */
    private String refreshToken;

    /** The timestamp indicating when the access token will expire. */
    private Instant expirationDate;
}

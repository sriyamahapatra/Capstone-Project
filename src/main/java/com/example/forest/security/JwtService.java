package com.example.forest.security;

import com.example.forest.Exceptions.CustomException;
import com.example.forest.document.MongoUserDocument;
import com.example.forest.repository.mongodb.MongoUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * JwtService.java
 *
 * Service responsible for creating, validating, and decoding JWT tokens.
 * <p>
 * This class manages both access and refresh tokens, embedding essential
 * user details such as username and roles, ensuring secure stateless
 * authentication across the application.
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    /** Claim key used to store user roles or scopes in JWT payload. */
    private static final String SCOPE_CLAIM = "scope";

    /** The token issuer name for identifying the source of the JWT. */
    private static final String ISSUER = "self";

    /** Claim key used to differentiate refresh tokens from access tokens. */
    private static final String REFRESH_CLAIM = "refresh";

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final MongoUserRepository userRepository;

    /** Token expiration duration (in milliseconds), configured via environment variable. */
    @Value("${jwt.expiration.time}")
    private Long jwtExpirationInMillis;

    /**
     * Generates a JWT access token for an authenticated user.
     *
     * @param authentication the {@link Authentication} object containing user details.
     * @return a signed JWT access token.
     */
    public String generateToken(Authentication authentication) {
        User principal = (User) authentication.getPrincipal();
        return generateTokenWithUserName(principal.getUsername());
    }

    /**
     * Generates a JWT access token for a given username.
     * <p>
     * Includes role and expiration claims based on the configuration.
     *
     * @param username the username for which to generate the token.
     * @return a signed JWT access token string.
     */
    public String generateTokenWithUserName(String username) {
        MongoUserDocument user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found: " + username));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusMillis(jwtExpirationInMillis))
                .subject(username)
                .claim(SCOPE_CLAIM, user.getRole()) // Store user role as claim
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    /**
     * Generates a refresh token for a given username.
     * <p>
     * The refresh token includes a "refresh" claim and a fixed expiration period of one day.
     *
     * @param username the username for which to generate the refresh token.
     * @return a signed JWT refresh token string.
     */
    public String generateRefreshToken(String username) {
        MongoUserDocument user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found: " + username));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(username)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(Duration.ofDays(1)))
                .claim(SCOPE_CLAIM, user.getRole())
                .claim(REFRESH_CLAIM, true)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    /**
     * Validates a JWT token by checking its username and expiration.
     *
     * @param token       the JWT token string.
     * @param userDetails the {@link UserDetails} associated with the token.
     * @return {@code true} if the token is valid, otherwise {@code false}.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Checks if a JWT token has expired.
     *
     * @param token the JWT token to validate.
     * @return {@code true} if expired, {@code false} otherwise.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).isBefore(Instant.now());
    }

    /**
     * Extracts the expiration timestamp from a JWT token.
     *
     * @param token the JWT token string.
     * @return the {@link Instant} representing token expiration.
     */
    private Instant extractExpiration(String token) {
        return jwtDecoder.decode(token).getExpiresAt();
    }

    /**
     * Extracts the username (subject) from a JWT token.
     *
     * @param token the JWT token string.
     * @return the username contained in the token's subject claim.
     */
    public String extractUsername(String token) {
        Jwt parsedJwt = jwtDecoder.decode(token);
        return parsedJwt.getSubject();
    }

    /**
     * Retrieves the configured JWT expiration time (in milliseconds).
     *
     * @return the configured token expiration duration.
     */
    public Long getJwtExpirationInMillis() {
        return jwtExpirationInMillis;
    }
}

package com.example.forest.service;

import com.example.forest.Exceptions.CustomException;
import com.example.forest.document.MongoRefreshTokenDocument;
import com.example.forest.repository.mongodb.MongoRefreshTokenRepository;
import com.example.forest.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

/**
 * Service responsible for handling the lifecycle of refresh tokens.
 *
 * <p>This includes token creation, validation, and deletion.
 * Tokens are persisted in MongoDB and are linked to a user's session for authentication renewal.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final MongoRefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    /**
     * Generates and stores a new refresh token for a given user.
     *
     * @param username The username for which to create the refresh token.
     * @return The saved {@link MongoRefreshTokenDocument} object containing the token and expiry.
     */
    @Transactional
    public MongoRefreshTokenDocument generateRefreshToken(String username) {
        if (username == null || username.isBlank()) {
            throw new CustomException("Username cannot be null or empty when generating a refresh token.");
        }

        String token = jwtService.generateRefreshToken(username);

        MongoRefreshTokenDocument refreshToken = new MongoRefreshTokenDocument();
        refreshToken.setToken(token);
        refreshToken.setExpiryDate(Instant.now().plus(Duration.ofDays(1)));

        MongoRefreshTokenDocument savedToken = refreshTokenRepository.save(refreshToken);

        log.info("✅ Refresh token generated successfully for user '{}'. Expiry: {}", username, savedToken.getExpiryDate());
        return savedToken;
    }

    /**
     * Validates whether a given refresh token exists and is still valid.
     *
     * @param refreshToken The token to validate.
     * @throws CustomException if the token is invalid, missing, or expired.
     */
    @Transactional(readOnly = true)
    public void validateRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CustomException("Refresh token cannot be null or empty.");
        }

        MongoRefreshTokenDocument tokenDoc = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new CustomException("Invalid refresh token: " + refreshToken));

        if (tokenDoc.getExpiryDate().isBefore(Instant.now())) {
            log.warn("⚠️ Refresh token expired at {}", tokenDoc.getExpiryDate());
            throw new CustomException("Refresh token has expired.");
        }

        log.debug("✅ Refresh token validated successfully: {}", refreshToken);
    }

    /**
     * Deletes a stored refresh token from the database.
     *
     * @param refreshToken The token string to be deleted.
     */
    @Transactional
    public void deleteRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CustomException("Refresh token cannot be null or empty for deletion.");
        }

        refreshTokenRepository.deleteByToken(refreshToken);
        log.info("🗑️ Refresh token deleted successfully.");
    }
}

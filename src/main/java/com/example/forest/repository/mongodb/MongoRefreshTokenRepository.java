package com.example.forest.repository.mongodb;

import com.example.forest.document.MongoRefreshTokenDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * MongoRefreshTokenRepository.java
 *
 * Repository interface for performing CRUD operations and token-based lookups
 * on {@link MongoRefreshTokenDocument} entities stored in MongoDB.
 * <p>
 * This repository is responsible for managing refresh tokens used in JWT-based
 * authentication, allowing token validation and invalidation during user sessions.
 */
public interface MongoRefreshTokenRepository extends MongoRepository<MongoRefreshTokenDocument, String> {

    /**
     * Finds a refresh token by its unique token value.
     *
     * @param token the refresh token string to search for.
     * @return an {@link Optional} containing the {@link MongoRefreshTokenDocument} if found,
     *         or empty if no matching token exists.
     */
    Optional<MongoRefreshTokenDocument> findByToken(String token);

    /**
     * Deletes a refresh token by its token value.
     * <p>
     * This method is typically called when a user logs out or the token expires.
     *
     * @param token the refresh token string to delete.
     */
    void deleteByToken(String token);
}

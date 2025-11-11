package com.example.forest.repository.mongodb;

import com.example.forest.document.MongoVerificationTokenDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * MongoVerificationTokenRepository.java
 *
 * Repository interface for managing {@link MongoVerificationTokenDocument} entities in MongoDB.
 * <p>
 * Provides methods to handle verification tokens used for user account activation,
 * password reset, and other token-based verification workflows.
 * <p>
 * Extends {@link MongoRepository}, giving access to CRUD operations while
 * also defining custom query methods for token lookup and deletion.
 */
public interface MongoVerificationTokenRepository extends MongoRepository<MongoVerificationTokenDocument, String> {

    /**
     * Finds a verification token by its unique token string.
     *
     * @param token the verification token string to search for.
     * @return an {@link Optional} containing the {@link MongoVerificationTokenDocument} if found,
     *         or empty if no match exists.
     */
    Optional<MongoVerificationTokenDocument> findByToken(String token);

    /**
     * Deletes a verification token by its token string.
     * <p>
     * This method is typically called once a token has been used or has expired.
     *
     * @param token the verification token string to delete.
     * @return an {@link Optional} containing the deleted {@link MongoVerificationTokenDocument},
     *         if it existed before deletion.
     */
    Optional<MongoVerificationTokenDocument> deleteByToken(String token);
}

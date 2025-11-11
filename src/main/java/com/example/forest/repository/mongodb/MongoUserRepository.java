package com.example.forest.repository.mongodb;

import com.example.forest.document.MongoUserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * MongoUserRepository.java
 *
 * Repository interface for managing {@link MongoUserDocument} entities in MongoDB.
 * <p>
 * Extends {@link MongoRepository} to provide standard CRUD operations along with
 * additional query methods for finding users by username or email.
 * <p>
 * This repository forms the backbone of the authentication and user management
 * functionality, supporting login, registration, and profile operations.
 */
public interface MongoUserRepository extends MongoRepository<MongoUserDocument, String> {

    /**
     * Finds a user by their unique username.
     * <p>
     * Commonly used during authentication and profile lookups.
     *
     * @param username the username of the user to search for.
     * @return an {@link Optional} containing the {@link MongoUserDocument} if found,
     *         or empty if no matching user exists.
     */
    Optional<MongoUserDocument> findByUsername(String username);

    List<MongoUserDocument> findAllByUsername(String username);

    void deleteAllByUsername(String username);

    /**
     * Finds a user by their registered email address.
     * <p>
     * Used during registration validation and password reset operations.
     *
     * @param email the email address of the user to search for.
     * @return an {@link Optional} containing the {@link MongoUserDocument} if found,
     *         or empty if no matching email exists.
     */
    Optional<MongoUserDocument> findByEmail(String email);
}

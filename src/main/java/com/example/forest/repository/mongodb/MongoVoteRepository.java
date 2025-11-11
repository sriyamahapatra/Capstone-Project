package com.example.forest.repository.mongodb;

import com.example.forest.document.MongoPostDocument;
import com.example.forest.document.MongoUserDocument;
import com.example.forest.document.MongoVoteDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * MongoVoteRepository.java
 *
 * Repository interface for managing {@link MongoVoteDocument} entities in MongoDB.
 * <p>
 * This repository handles CRUD operations and custom queries related to voting activity
 * on posts. It is primarily used by the voting service to record, retrieve, and remove
 * user votes.
 * <p>
 * Extends {@link MongoRepository} to inherit built-in persistence functionality
 * while also defining domain-specific query methods.
 */
public interface MongoVoteRepository extends MongoRepository<MongoVoteDocument, String> {

    /**
     * Finds the most recent vote (if any) made by a user on a specific post.
     * <p>
     * This is useful for determining whether a user has already voted and what
     * type of vote they cast (upvote or downvote).
     *
     * @param post the {@link MongoPostDocument} on which the vote was made.
     * @param user the {@link MongoUserDocument} who cast the vote.
     * @return an {@link Optional} containing the latest {@link MongoVoteDocument} if found,
     *         or empty if the user has not voted on this post.
     */
    Optional<MongoVoteDocument> findTopByPostAndUserOrderByIdDesc(MongoPostDocument post, MongoUserDocument user);

    /**
     * Deletes a vote associated with a specific post and user.
     * <p>
     * Typically used when a user retracts or changes their vote.
     *
     * @param post the {@link MongoPostDocument} representing the post.
     * @param user the {@link MongoUserDocument} representing the user.
     */
    void deleteByPostAndUser(MongoPostDocument post, MongoUserDocument user);
}

package com.example.forest.repository.mongodb;

import com.example.forest.document.MongoSubredditDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * MongoSubredditRepository.java
 *
 * Repository interface for managing {@link MongoSubredditDocument} entities in MongoDB.
 * <p>
 * Extends {@link MongoRepository} to provide standard CRUD operations,
 * along with custom query methods for finding subreddits by name and collections of names.
 * <p>
 * This repository is used primarily by services that handle subreddit creation,
 * retrieval, and subscription management.
 */
public interface MongoSubredditRepository extends MongoRepository<MongoSubredditDocument, String> {

    /**
     * Finds a subreddit by its exact name.
     *
     * @param subredditName the name of the subreddit to look up.
     * @return an {@link Optional} containing the {@link MongoSubredditDocument} if found,
     *         or empty if no match exists.
     */
    Optional<MongoSubredditDocument> findByName(String subredditName);

    /**
     * Finds a subreddit by its name, ignoring case sensitivity.
     * <p>
     * Useful for handling user input that may vary in capitalization.
     *
     * @param subredditName the name of the subreddit (case-insensitive).
     * @return an {@link Optional} containing the matching {@link MongoSubredditDocument} if found.
     */
    Optional<MongoSubredditDocument> findByNameIgnoreCase(String subredditName);

    /**
     * Retrieves all subreddits whose names match any in the given list.
     * <p>
     * Often used for fetching multiple subreddits when users subscribe
     * or follow multiple communities at once.
     *
     * @param names a list of subreddit names to search for.
     * @return a list of matching {@link MongoSubredditDocument} objects.
     */
    List<MongoSubredditDocument> findAllByNameIn(List<String> names);
}

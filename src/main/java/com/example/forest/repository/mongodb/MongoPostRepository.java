package com.example.forest.repository.mongodb;

import com.example.forest.document.MongoPostDocument;
import com.example.forest.document.MongoSubredditDocument;
import com.example.forest.document.MongoUserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * MongoPostRepository.java
 *
 * Repository interface for managing {@link MongoPostDocument} entities in MongoDB.
 * <p>
 * Extends {@link MongoRepository}, providing standard CRUD operations and
 * several custom query methods for retrieving posts by name, subreddit, or user.
 */
public interface MongoPostRepository extends MongoRepository<MongoPostDocument, String> {

    /**
     * Finds posts whose names contain the given search query (case-insensitive).
     *
     * @param query the text fragment to search for in post names.
     * @return a list of {@link MongoPostDocument} objects whose names match the query.
     */
    List<MongoPostDocument> findByPostNameContainingIgnoreCase(String query);

    /**
     * Retrieves all posts belonging to a specific subreddit.
     *
     * @param subreddit the {@link MongoSubredditDocument} whose posts should be fetched.
     * @return a list of {@link MongoPostDocument} objects under the given subreddit.
     */
    List<MongoPostDocument> findAllBySubreddit(MongoSubredditDocument subreddit);

    /**
     * Retrieves all posts created by a specific user.
     *
     * @param user the {@link MongoUserDocument} representing the author.
     * @return a list of {@link MongoPostDocument} objects authored by the given user.
     */
    List<MongoPostDocument> findAllByUser(MongoUserDocument user);
}

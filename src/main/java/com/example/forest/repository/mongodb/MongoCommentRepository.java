package com.example.forest.repository.mongodb;

import com.example.forest.document.MongoCommentDocument;
import com.example.forest.document.MongoPostDocument;
import com.example.forest.document.MongoUserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * MongoCommentRepository.java
 *
 * Repository interface for performing CRUD operations and custom queries
 * on {@link MongoCommentDocument} entities stored in MongoDB.
 * <p>
 * Extends {@link MongoRepository}, which provides built-in support for
 * common database operations such as save, delete, and find.
 */
public interface MongoCommentRepository extends MongoRepository<MongoCommentDocument, String> {

    /**
     * Finds all comments associated with a specific post.
     *
     * @param post the {@link MongoPostDocument} whose comments are to be retrieved.
     * @return a list of {@link MongoCommentDocument} objects related to the given post.
     */
    List<MongoCommentDocument> findByPost(MongoPostDocument post);

    /**
     * Deletes all comments associated with a specific post.
     *
     * @param post the {@link MongoPostDocument} whose comments should be removed.
     */
    void deleteAllByPost(MongoPostDocument post);

    /**
     * Finds all comments created by a specific user.
     *
     * @param user the {@link MongoUserDocument} whose comments are to be retrieved.
     * @return a list of {@link MongoCommentDocument} objects created by the given user.
     */
    List<MongoCommentDocument> findAllByUser(MongoUserDocument user);
}

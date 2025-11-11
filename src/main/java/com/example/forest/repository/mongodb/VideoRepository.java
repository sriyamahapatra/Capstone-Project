package com.example.forest.repository.mongodb;

import com.example.forest.document.Video;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * VideoRepository.java
 *
 * Repository interface for performing CRUD operations on {@link Video} entities in MongoDB.
 * <p>
 * This repository manages video data persistence, allowing upload, retrieval,
 * and deletion of video files stored in the database.
 * <p>
 * Extends {@link MongoRepository}, providing built-in methods such as
 * {@code save()}, {@code findById()}, {@code findAll()}, and {@code deleteById()}.
 */
public interface VideoRepository extends MongoRepository<Video, String> {
}

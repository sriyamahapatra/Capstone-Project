package com.example.forest.repository.mongodb;

import com.example.forest.document.Photo;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * PhotoRepository.java
 *
 * Repository interface for performing CRUD operations on {@link Photo} entities in MongoDB.
 * <p>
 * This repository provides the persistence layer for managing uploaded photos,
 * including storing, retrieving, and deleting image data.
 * <p>
 * Extends {@link MongoRepository}, which supplies standard database operations such as
 * {@code save()}, {@code findById()}, {@code findAll()}, and {@code deleteById()}.
 */
public interface PhotoRepository extends MongoRepository<Photo, String> {
}

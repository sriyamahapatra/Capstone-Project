package com.example.forest.service;

import com.example.forest.Exceptions.CustomException;
import com.example.forest.document.Photo;
import com.example.forest.repository.mongodb.PhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

/**
 * Service layer responsible for managing photo uploads and retrieval.
 *
 * Features:
 *  - Stores images in MongoDB as binary data.
 *  - Retrieves stored photos by their unique ID.
 *  - Provides error handling and safe fallbacks for file processing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoService {

    private final PhotoRepository photoRepository;

    /**
     * Uploads and stores a new photo in the database.
     *
     * @param title The title or label for the photo.
     * @param file  The uploaded image file as a {@link MultipartFile}.
     * @return The generated unique ID of the saved photo.
     * @throws IOException if the uploaded file cannot be read.
     */
    public String addPhoto(String title, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new CustomException("Uploaded photo file is empty or missing.");
        }

        // Create a new Photo document and set its properties
        Photo photo = new Photo();
        photo.setTitle(title);
        photo.setImage(new Binary(BsonBinarySubType.BINARY, file.getBytes()));

        // Insert photo into MongoDB
        Photo savedPhoto = photoRepository.insert(photo);
        log.info("Photo '{}' uploaded successfully with ID: {}", title, savedPhoto.getId());

        return savedPhoto.getId();
    }

    /**
     * Retrieves a stored photo from the database by its ID.
     *
     * @param id The unique photo ID.
     * @return A {@link Photo} object containing the stored image and metadata.
     */
    public Photo getPhoto(String id) {
        if (id == null || id.isBlank()) {
            throw new CustomException("Invalid photo ID provided.");
        }

        Optional<Photo> optionalPhoto = photoRepository.findById(id);
        if (optionalPhoto.isEmpty()) {
            log.warn("No photo found with ID: {}", id);
            throw new CustomException("No photo found with ID: " + id);
        }

        log.info("Fetched photo with ID: {}", id);
        return optionalPhoto.get();
    }
}

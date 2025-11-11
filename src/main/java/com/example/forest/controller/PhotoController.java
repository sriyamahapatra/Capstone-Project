package com.example.forest.controller;

import com.example.forest.document.Photo;
import com.example.forest.service.PhotoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * PhotoController.java
 *
 * Manages photo upload and retrieval operations.
 * Supports uploading photos as multipart files and retrieving
 * stored images directly as binary data (JPEG format).
 *
 * Base endpoint: /api/v1/photos
 */
@RestController
@RequestMapping("/api/v1/photos")
public class PhotoController {

    private final PhotoService photoService;

    /**
     * Injects the PhotoService dependency for managing photo storage and retrieval.
     */
    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    /**
     * Uploads a new photo to the database.
     *
     * @param title A short title or description for the photo.
     * @param image The image file to be uploaded (expected as multipart form data).
     * @return A response containing the photo's ID and access URL.
     *
     * Flow:
     *  1. The image file is saved via the PhotoService.
     *  2. The system generates a public access URL for the uploaded photo.
     *  3. Returns the ID and URL in JSON format with HTTP 201 (Created).
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> addPhoto(
            @RequestParam("title") String title,
            @RequestParam("image") MultipartFile image) throws IOException {

        // Save photo in the database and retrieve its generated ID
        String id = photoService.addPhoto(title, image);

        // Build a public access URL using the current context path
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/photos/")
                .path(id)
                .toUriString();

        // Prepare response body
        Map<String, String> response = new HashMap<>();
        response.put("id", id);
        response.put("url", fileDownloadUri);

        // Return 201 (Created) with ID and access URL
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves a stored photo by its ID.
     *
     * @param id The unique identifier of the photo.
     * @return The image as binary data (JPEG format) or 404 if not found.
     *
     * Notes:
     *  - Sets a 1-week cache policy to improve performance for frequent requests.
     *  - Returns image bytes directly in the response body.
     */
    @GetMapping(value = "/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getPhoto(@PathVariable String id) {
        Photo photo = photoService.getPhoto(id);

        // Handle missing or invalid photo entries
        if (photo == null || photo.getImage() == null) {
            return ResponseEntity.notFound().build();
        }

        // Return image data with caching headers
        return ResponseEntity.ok()
                .header("Cache-Control", "public, max-age=604800") // Cache for 1 week
                .body(photo.getImage().getData());
    }
}

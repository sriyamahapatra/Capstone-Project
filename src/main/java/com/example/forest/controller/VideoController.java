package com.example.forest.controller;

import com.example.forest.dto.VideoResponse;
import com.example.forest.service.VideoService;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

/**
 * VideoController.java
 *
 * Handles video upload, retrieval, and streaming operations.
 * Uses MongoDB GridFS to store large video files and supports streaming
 * directly through the REST API.
 *
 * Base endpoint: /api/v1/videos
 */
@RestController
@RequestMapping("/api/v1/videos")
public class VideoController {

    private final VideoService videoService;

    /**
     * Injects the VideoService dependency used for handling video operations.
     */
    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    /**
     * Uploads a new video file.
     *
     * @param title The title or name associated with the video.
     * @param file  The video file to upload (multipart format).
     * @return A response containing the uploaded video’s ID and its access URL.
     *
     * Flow:
     *  1. The video file is saved in MongoDB GridFS via VideoService.
     *  2. A download/streaming URL is dynamically generated.
     *  3. Returns a JSON response with the file ID and its public access URL.
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, String>> addVideo(
            @RequestParam("title") String title,
            @RequestParam("file") MultipartFile file) throws IOException {

        // Save video to MongoDB GridFS and get generated file ID
        String id = videoService.addVideo(title, file);

        // Construct a streaming URL for the uploaded video
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/videos/stream/")
                .path(id)
                .toUriString();

        // Build JSON response containing video details
        Map<String, String> response = new HashMap<>();
        response.put("id", id);
        response.put("url", fileDownloadUri);

        // Return 201 (Created) with video ID and URL
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves metadata for a specific video.
     *
     * @param id The unique identifier of the video.
     * @return A {@link VideoResponse} object containing metadata,
     *         or 404 if the video does not exist.
     */
    @GetMapping("/{id}")
    public ResponseEntity<VideoResponse> getVideo(@PathVariable String id) {
        VideoResponse video = videoService.getVideo(id);

        // Handle missing video records
        if (video == null) {
            return ResponseEntity.notFound().build();
        }

        // Return video metadata
        return ResponseEntity.ok(video);
    }

    /**
     * Streams a stored video directly to the client.
     *
     * @param id The unique ID of the video to stream.
     * @return Video content as binary data with proper MIME type,
     *         or 404 if the video is not found.
     *
     * Notes:
     *  - Uses GridFS for efficient large file streaming.
     *  - Sets content type dynamically based on stored metadata.
     */
    @GetMapping(value = "/stream/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> streamVideo(@PathVariable String id) throws IOException {
        GridFSFile file = videoService.getFile(id);

        // Return 404 if no matching video is found
        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        // Convert GridFS file into a resource stream
        GridFsResource resource = videoService.getResource(file);

        // Read video bytes from the input stream
        byte[] videoBytes = resource.getInputStream().readAllBytes();

        // Return binary response with appropriate content type
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(file.getMetadata().get("_contentType").toString()))
                .body(videoBytes);
    }
}

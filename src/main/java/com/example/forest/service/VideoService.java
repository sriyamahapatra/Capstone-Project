package com.example.forest.service;

import com.example.forest.dto.VideoResponse;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;

/**
 * Service responsible for managing video uploads, retrieval, and streaming
 * using MongoDB GridFS.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VideoService {

    private final GridFsTemplate gridFsTemplate;
    private final GridFsOperations gridFsOperations;

    /**
     * Uploads a new video file to MongoDB GridFS and returns its generated ID.
     *
     * @param title The video title
     * @param file  The video file as a MultipartFile
     * @return The stored file’s MongoDB ObjectId as a string
     * @throws IOException if an I/O error occurs during upload
     */
    public String addVideo(String title, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Video file cannot be null or empty.");
        }

        log.info("🎥 Uploading video: '{}' (size: {} bytes)", title, file.getSize());

        DBObject metaData = new BasicDBObject();
        metaData.put("type", "video");
        metaData.put("title", title != null ? title : "Untitled");

        ObjectId id = gridFsTemplate.store(
                file.getInputStream(),
                file.getOriginalFilename(), // preserve original name
                file.getContentType(),
                metaData
        );

        log.info("✅ Video uploaded successfully. ID: {}", id.toString());
        return id.toString();
    }

    /**
     * Retrieves metadata and download URL for a video file by ID.
     *
     * @param id The video file’s MongoDB ObjectId
     * @return {@link VideoResponse} containing metadata and URL, or null if not found
     */
    public VideoResponse getVideo(String id) {
        if (id == null || id.isBlank()) {
            log.warn("Attempted to fetch video with null/empty ID.");
            return null;
        }

        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));

        if (file == null) {
            log.warn("No video found for ID: {}", id);
            return null;
        }

        String title = file.getMetadata() != null && file.getMetadata().get("title") != null
                ? file.getMetadata().get("title").toString()
                : "Untitled";

        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/videos/stream/")
                .path(id)
                .toUriString();

        log.info("🎬 Video fetched: '{}' (ID: {})", title, id);
        return new VideoResponse(id, title, url);
    }

    /**
     * Retrieves the raw {@link GridFSFile} for a given video ID.
     *
     * @param id The video file’s MongoDB ObjectId
     * @return The corresponding {@link GridFSFile}, or null if not found
     */
    public GridFSFile getFile(String id) {
        if (id == null || id.isBlank()) {
            log.warn("Attempted to fetch file with null/empty ID.");
            return null;
        }

        return gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));
    }

    /**
     * Converts a {@link GridFSFile} into a {@link GridFsResource} stream for playback or download.
     *
     * @param file The GridFS file to be streamed
     * @return The resource representation of the file
     */
    public GridFsResource getResource(GridFSFile file) {
        if (file == null) {
            throw new IllegalArgumentException("GridFSFile cannot be null.");
        }
        return gridFsOperations.getResource(file);
    }
}

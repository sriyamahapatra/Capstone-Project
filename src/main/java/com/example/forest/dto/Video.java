package com.example.forest.dto;

import lombok.Data;
import java.io.InputStream;

/**
 * Video.java
 *
 * Data Transfer Object (DTO) representing video data being sent or streamed.
 * Typically used for handling video upload and retrieval operations,
 * providing a streamable input for large media files.
 */
@Data
public class Video {

    /** The title or name associated with the video. */
    private String title;

    /** The input stream used to read or stream the video content. */
    private InputStream stream;
}

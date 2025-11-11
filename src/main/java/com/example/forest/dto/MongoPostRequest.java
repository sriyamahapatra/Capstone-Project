package com.example.forest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MongoPostRequest.java
 *
 * Data Transfer Object (DTO) used for creating or updating posts.
 * Captures all relevant information required to persist or modify a post
 * in the MongoDB database, including media and subreddit association.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MongoPostRequest {

    /** Unique identifier of the post (used during updates). */
    private String postId;

    /** The name of the subreddit where the post will be published. */
    private String subredditName;

    /** The title or main heading of the post (required). */
    @NotBlank(message = "Post name cannot be empty or Null")
    private String postName;

    /** Optional external URL associated with the post. */
    private String url;

    /** The main textual content or description of the post. */
    private String description;

    /** ID of the associated photo (if any). */
    private String photoId;

    /** ID of the associated video (if any). */
    private String videoId;
}

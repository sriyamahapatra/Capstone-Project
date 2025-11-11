package com.example.forest.dto;

import com.example.forest.document.Photo;
import com.example.forest.document.Video;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * PostResponse.java
 *
 * Data Transfer Object (DTO) representing the post details sent from the server
 * to the client. Combines metadata, user information, voting status, and any
 * attached media (photo or video).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {

    /** Unique identifier of the post. */
    private String id;

    /** The title or main heading of the post. */
    private String postName;

    /** Optional external URL linked to the post. */
    private String url;

    /** The main textual content or description of the post. */
    private String description;

    /** Username of the post's creator. */
    private String userName;

    /** Name of the subreddit where the post was published. */
    private String subredditName;

    /** Net vote count for the post (upvotes - downvotes). */
    private Integer voteCount;

    /** Total number of comments on the post. */
    private Integer commentCount;

    /** Human-readable time since creation (e.g., "3 hours ago"). */
    private String duration;

    /** Indicates whether the current user has upvoted this post. */
    private boolean upVote;

    /** Indicates whether the current user has downvoted this post. */
    private boolean downVote;

    /** Timestamp representing when the post was created. */
    private Instant createdDate;

    /** Embedded photo object attached to the post (if any). */
    private Photo photo;

    /** Embedded video object attached to the post (if any). */
    private Video video;
}

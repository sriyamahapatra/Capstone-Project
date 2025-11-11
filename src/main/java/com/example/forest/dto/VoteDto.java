package com.example.forest.dto;

import com.example.forest.model.VoteType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * VoteDto.java
 *
 * Data Transfer Object (DTO) used for submitting or processing votes on posts.
 * Encapsulates the type of vote (upvote or downvote) and the corresponding post ID.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VoteDto {

    /** The type of vote being made — UPVOTE or DOWNVOTE. */
    private VoteType voteType;

    /** The unique identifier of the post being voted on. */
    private String postId;
}

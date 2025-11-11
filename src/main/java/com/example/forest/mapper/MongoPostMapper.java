package com.example.forest.mapper;

import com.example.forest.document.*;
import com.example.forest.dto.MongoPostRequest;
import com.example.forest.dto.PostResponse;
import com.example.forest.repository.mongodb.MongoCommentRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * MongoPostMapper.java
 *
 * A MapStruct-based mapper responsible for converting between
 * {@link MongoPostDocument} entities and {@link PostResponse} / {@link MongoPostRequest} DTOs.
 * <p>
 * This abstraction eliminates the need for manual mapping logic and keeps
 * entity-to-DTO transformations clean, consistent, and maintainable.
 */
@Mapper(componentModel = "spring")
public abstract class MongoPostMapper {

    /** Repository used to fetch comment counts for a given post. */
    @Autowired
    private MongoCommentRepository commentRepository;

    /**
     * Maps a {@link MongoPostRequest} to a {@link MongoPostDocument}.
     * <p>
     * This method is used during post creation or update, converting
     * client-side DTO data into a MongoDB-compatible entity.
     *
     * @param postRequest the DTO containing post details from the client.
     * @param subreddit   the {@link MongoSubredditDocument} the post belongs to.
     * @param user        the {@link MongoUserDocument} who created the post.
     * @param photo       the {@link Photo} object attached to the post (if any).
     * @param video       the {@link Video} object attached to the post (if any).
     * @return a {@link MongoPostDocument} ready for persistence.
     */
    @Mapping(target = "id", source = "postRequest.postId")
    @Mapping(target = "subreddit", source = "subreddit")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "createdDate", expression = "java(java.time.Instant.now())")
    @Mapping(target = "description", source = "postRequest.description")
    @Mapping(target = "photo", source = "photo")
    @Mapping(target = "video", source = "video")
    @Mapping(target = "voteCount", constant = "0")
    @Mapping(target = "notificationStatus", constant = "false")
    public abstract MongoPostDocument map(
            MongoPostRequest postRequest,
            MongoSubredditDocument subreddit,
            MongoUserDocument user,
            Photo photo,
            Video video
    );

    /**
     * Maps a {@link MongoPostDocument} entity to a {@link PostResponse} DTO.
     * <p>
     * This method prepares the data to be returned to the client, including
     * derived fields like comment count and related user/subreddit information.
     *
     * @param post the {@link MongoPostDocument} fetched from MongoDB.
     * @return a fully populated {@link PostResponse} object for API output.
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "postName", source = "postName")
    @Mapping(target = "subredditName", source = "subreddit.name")
    @Mapping(target = "userName", source = "user.username")
    @Mapping(target = "photo", source = "photo")
    @Mapping(target = "video", source = "video")
    @Mapping(target = "commentCount", expression = "java(commentCount(post))")
    @Mapping(target = "duration", ignore = true)
    @Mapping(target = "upVote", ignore = true)
    @Mapping(target = "downVote", ignore = true)
    public abstract PostResponse mapToDto(MongoPostDocument post);

    /**
     * Utility method to calculate the total number of comments associated with a post.
     *
     * @param post the {@link MongoPostDocument} whose comments are being counted.
     * @return the total number of comments for the given post.
     */
    Integer commentCount(MongoPostDocument post) {
        return commentRepository.findByPost(post).size();
    }
}

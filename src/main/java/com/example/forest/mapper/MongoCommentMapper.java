package com.example.forest.mapper;

import com.example.forest.document.MongoCommentDocument;
import com.example.forest.document.MongoPostDocument;
import com.example.forest.document.MongoUserDocument;
import com.example.forest.dto.CommentsDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MongoCommentMapper.java
 *
 * A MapStruct mapper interface for converting between {@link MongoCommentDocument}
 * entities and {@link CommentsDto} objects.
 * <p>
 * This helps simplify object transformations between the persistence layer (MongoDB)
 * and the presentation layer (REST API), reducing manual boilerplate code.
 */
@Mapper(componentModel = "spring")
public interface MongoCommentMapper {

    /**
     * Maps a {@link CommentsDto} to a {@link MongoCommentDocument}.
     * <p>
     * This method is used when creating a new comment entity from a DTO,
     * associating it with a specific post and user.
     *
     * @param commentsDto the DTO containing the comment data from the client.
     * @param post        the {@link MongoPostDocument} representing the related post.
     * @param user        the {@link MongoUserDocument} representing the author of the comment.
     * @return a new {@link MongoCommentDocument} entity ready to be persisted in MongoDB.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "text", source = "commentsDto.text")
    @Mapping(target = "createdDate", expression = "java(java.time.Instant.now())")
    @Mapping(target = "post", source = "post")
    @Mapping(target = "user", source = "user")
    MongoCommentDocument map(CommentsDto commentsDto, MongoPostDocument post, MongoUserDocument user);

    /**
     * Maps a {@link MongoCommentDocument} entity to a {@link CommentsDto}.
     * <p>
     * This method is used when preparing comment data for API responses,
     * including details such as the post ID and username.
     *
     * @param comment the {@link MongoCommentDocument} retrieved from MongoDB.
     * @return a {@link CommentsDto} object ready to be sent to the client.
     */
    @Mapping(target = "postId", expression = "java(comment.getPost().getId())")
    @Mapping(target = "userName", expression = "java(comment.getUser().getUsername())")
    @Mapping(target = "duration", ignore = true)
    CommentsDto mapToDto(MongoCommentDocument comment);
}

package com.example.forest.mapper;

import com.example.forest.document.MongoSubredditDocument;
import com.example.forest.dto.SubredditDto;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MongoSubredditMapper.java
 *
 * A MapStruct mapper interface that handles conversion between
 * {@link MongoSubredditDocument} entities and {@link SubredditDto} objects.
 * <p>
 * It simplifies transformations between database models and API-level DTOs,
 * eliminating repetitive and error-prone manual mapping logic.
 */
@Mapper(componentModel = "spring")
public interface MongoSubredditMapper {

    /**
     * Converts a {@link MongoSubredditDocument} entity into a {@link SubredditDto}.
     * <p>
     * This mapping is used when preparing subreddit data for API responses.
     * It includes computed fields such as the number of posts and the username
     * of the subreddit’s creator.
     *
     * @param subreddit the {@link MongoSubredditDocument} retrieved from the database.
     * @return a {@link SubredditDto} object ready to be returned via the API.
     */
    @Mapping(target = "numberOfPosts", expression = "java(subreddit.getPosts() != null ? subreddit.getPosts().size() : 0)")
    @Mapping(target = "userName", source = "user.username")
    @Mapping(target = "duration", ignore = true)
    SubredditDto mapSubredditToDto(MongoSubredditDocument subreddit);

    /**
     * Converts a {@link SubredditDto} back into a {@link MongoSubredditDocument}.
     * <p>
     * This reverse mapping is typically used when creating or updating subreddits.
     * The {@link InheritInverseConfiguration} annotation reuses mapping rules
     * from {@link #mapSubredditToDto(MongoSubredditDocument)}, with some ignored fields.
     *
     * @param subredditDto the DTO containing subreddit creation or update data.
     * @return a {@link MongoSubredditDocument} ready for persistence.
     */
    @InheritInverseConfiguration
    @Mapping(target = "posts", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    MongoSubredditDocument mapDtoToSubreddit(SubredditDto subredditDto);
}

package com.example.forest.service;

import com.example.forest.Exceptions.CustomException;
import com.example.forest.document.MongoPostDocument;
import com.example.forest.document.MongoUserDocument;
import com.example.forest.dto.PostResponse;
import com.example.forest.mapper.MongoPostMapper;
import com.example.forest.repository.mongodb.MongoPostRepository;
import com.example.forest.repository.mongodb.MongoSubredditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service responsible for user-related functionalities.
 * <p>
 * Includes generating personalized user feeds based on:
 * - The user’s own posts
 * - Posts from subreddits they are interested in
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final AuthService authService;
    private final MongoPostRepository postRepository;
    private final MongoSubredditRepository subredditRepository;
    private final MongoPostMapper postMapper;

    /**
     * Retrieves a personalized feed for the currently authenticated user.
     * Combines posts created by the user with posts from subreddits they follow.
     *
     * @return a list of {@link PostResponse} objects representing the user’s feed
     */
    public List<PostResponse> getMyFeed() {
        MongoUserDocument currentUser = authService.getCurrentUser();

        if (currentUser == null) {
            throw new CustomException("User not authenticated. Please log in to view your feed.");
        }

        log.info("📬 Generating feed for user: {}", currentUser.getUsername());

        // ✅ Get user’s own posts
        List<MongoPostDocument> userPosts = postRepository.findAllByUser(currentUser);
        log.debug("Found {} posts created by user '{}'.", userPosts.size(), currentUser.getUsername());

        // ✅ Get posts from subreddits matching user interests
        Set<String> interests = Optional.ofNullable(currentUser.getInterests())
                .orElse(Collections.emptySet());

        List<MongoPostDocument> interestPosts = Collections.emptyList();
        if (!interests.isEmpty()) {
            log.debug("User '{}' has {} interests: {}", currentUser.getUsername(), interests.size(), interests);
            interestPosts = subredditRepository.findAllByNameIn(new ArrayList<>(interests))
                    .stream()
                    .peek(subreddit -> log.debug("Fetching posts for subreddit '{}'.", subreddit.getName()))
                    .flatMap(subreddit -> postRepository.findAllBySubreddit(subreddit).stream())
                    .collect(Collectors.toList());
            log.debug("Found {} posts from subscribed subreddits.", interestPosts.size());
        } else {
            log.info("User '{}' has no subreddit interests set.", currentUser.getUsername());
        }

        // ✅ Merge, deduplicate, and map to DTOs
        List<PostResponse> feed = Stream.concat(userPosts.stream(), interestPosts.stream())
                .distinct()
                .map(postMapper::mapToDto)
                .collect(Collectors.toList());

        log.info("✅ Final feed size for user '{}': {}", currentUser.getUsername(), feed.size());
        return feed;
    }
}

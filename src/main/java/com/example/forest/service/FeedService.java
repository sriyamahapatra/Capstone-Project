package com.example.forest.service;

import com.example.forest.document.MongoPostDocument;
import com.example.forest.document.MongoSubredditDocument;
import com.example.forest.document.MongoUserDocument;
import com.example.forest.dto.PostResponse;
import com.example.forest.mapper.MongoPostMapper;
import com.example.forest.repository.mongodb.MongoPostRepository;
import com.example.forest.repository.mongodb.MongoSubredditRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * FeedService.java
 *
 * Service responsible for generating a personalized feed for each user.
 * <p>
 * The feed combines:
 * <ul>
 *     <li>Posts created by the user</li>
 *     <li>Posts from subreddits matching the user’s selected interests</li>
 * </ul>
 * The result is a deduplicated and relevance-sorted stream of posts that the user
 * is most likely to engage with.
 */
@Service
@AllArgsConstructor
@Slf4j
public class FeedService {

    private final AuthService authService;
    private final MongoPostRepository postRepository;
    private final MongoSubredditRepository subredditRepository;
    private final MongoPostMapper postMapper;
    private final MongoTemplate mongoTemplate;

    /**
     * Builds and retrieves the personalized feed for the currently authenticated user.
     *
     * @return a list of {@link PostResponse} objects representing the user's feed.
     */
    public List<PostResponse> getMyFeed() {
        // Retrieve the currently logged-in user from the authentication context
        MongoUserDocument currentUser = authService.getCurrentUser();
        log.info("Fetching feed for user: {}", currentUser.getUsername());

        // Fetch all posts created by the user
        List<MongoPostDocument> userPosts = postRepository.findAllByUser(currentUser);
        log.info("Found {} posts created by user", userPosts.size());

        // Fetch posts based on user interests
        Set<String> interests = currentUser.getInterests();
        List<MongoPostDocument> interestPosts = new ArrayList<>();

        if (interests != null && !interests.isEmpty()) {
            log.info("User interests: {}", interests);

            // Convert interests into case-insensitive regex patterns for flexible matching
            List<Pattern> patterns = interests.stream()
                    .map(interest -> Pattern.compile(interest, Pattern.CASE_INSENSITIVE))
                    .collect(Collectors.toList());

            // Query all subreddits that match any of the user’s interests
            Query query = new Query();
            query.addCriteria(Criteria.where("name").in(patterns));

            mongoTemplate.find(query, MongoSubredditDocument.class)
                    .forEach(subreddit -> {
                        log.info("Fetching posts for subreddit: {}", subreddit.getName());
                        List<MongoPostDocument> posts = postRepository.findAllBySubreddit(subreddit);
                        log.info("Found {} posts for subreddit {}", posts.size(), subreddit.getName());
                        interestPosts.addAll(posts);
                    });

        } else {
            log.info("User has no interests. Showing personal posts only.");
        }

        // Combine user posts and interest-based posts, remove duplicates, and map to DTOs
        List<PostResponse> feed = Stream.concat(userPosts.stream(), interestPosts.stream())
                .distinct()
                .map(postMapper::mapToDto)
                .collect(Collectors.toList());

        log.info("Total posts in feed: {}", feed.size());
        return feed;
    }
}

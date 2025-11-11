package com.example.forest.service;

import com.example.forest.Exceptions.CustomException;
import com.example.forest.document.*;
import com.example.forest.dto.MongoPostRequest;
import com.example.forest.dto.PostResponse;
import com.example.forest.mapper.MongoPostMapper;
import com.example.forest.model.Role;
import com.example.forest.repository.mongodb.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for handling all post-related operations in the Forest application.
 *
 * This includes:
 *  - Creating posts (with content moderation)
 *  - Fetching posts by subreddit, user, or search query
 *  - Updating and deleting posts (with role-based access control)
 *  - Managing notification preferences for posts
 */
@Service
@AllArgsConstructor
@Slf4j
public class MongoPostService {

    private final MongoSubredditRepository subredditRepository;
    private final AuthService authService;
    private final MongoPostMapper postMapper;
    private final MongoPostRepository postRepository;
    private final MongoUserRepository userRepository;
    private final MongoCommentRepository commentRepository;
    private final PhotoRepository photoRepository;
    private final VideoRepository videoRepository;
    private final ContentModerationService contentModerationService;

    /**
     * Creates and saves a new post after performing content moderation on text, images, and videos.
     *
     * @param postRequest The post request containing all details.
     * @return A DTO representing the saved post.
     */
    @Transactional
    public PostResponse save(MongoPostRequest postRequest) {
        // Combine text content for moderation
        String combinedText = postRequest.getPostName() + " " + postRequest.getDescription();

        // Moderate text content
        if (contentModerationService.isContentInappropriate(combinedText)) {
            throw new CustomException("Your post contains inappropriate content and cannot be saved.");
        }

        // Moderate image content if attached
        Photo photo = postRequest.getPhotoId() != null
                ? photoRepository.findById(postRequest.getPhotoId()).orElse(null)
                : null;
        if (photo != null && contentModerationService.isContentInappropriate(photo.getImage(), "image/jpeg")) {
            throw new CustomException("The uploaded image is inappropriate and cannot be saved.");
        }

        // Moderate video content if attached
        Video video = postRequest.getVideoId() != null
                ? videoRepository.findById(postRequest.getVideoId()).orElse(null)
                : null;
        if (video != null && contentModerationService.isContentInappropriate(video.getVideo(), "video/mp4")) {
            throw new CustomException("The uploaded video is inappropriate and cannot be saved.");
        }

        // Retrieve subreddit and current user
        MongoSubredditDocument subreddit = subredditRepository.findByName(postRequest.getSubredditName())
                .orElseThrow(() -> new CustomException("Subreddit not found: " + postRequest.getSubredditName()));
        MongoUserDocument currentUser = authService.getCurrentUser();

        // Map post request to a MongoPostDocument and save it
        MongoPostDocument savedPost = postRepository.save(postMapper.map(postRequest, subreddit, currentUser, photo, video));

        // Build and assign a URL to the post
        String POST_URL = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/mongo/posts/" + savedPost.getId())
                .toUriString();
        savedPost.setUrl(POST_URL);
        postRepository.save(savedPost);

        return postMapper.mapToDto(savedPost);
    }

    /**
     * Retrieves all posts.
     *
     * @return List of all posts mapped to DTOs.
     */
    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts() {
        return postRepository.findAll()
                .stream()
                .map(postMapper::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single post by ID.
     *
     * @param id The post ID.
     * @return A DTO containing post details.
     */
    @Transactional(readOnly = true)
    public PostResponse getPost(String id) {
        MongoPostDocument post = postRepository.findById(id)
                .orElseThrow(() -> new CustomException("Post with ID not found: " + id));
        return postMapper.mapToDto(post);
    }

    /**
     * Retrieves posts by subreddit ID.
     *
     * @param subredditId The subreddit ID.
     * @return A list of posts under the specified subreddit.
     */
    @Transactional(readOnly = true)
    public List<PostResponse> getPostsBySubreddit(String subredditId) {
        MongoSubredditDocument subreddit = subredditRepository.findById(subredditId)
                .orElseThrow(() -> new CustomException("Subreddit not found: " + subredditId));

        return postRepository.findAllBySubreddit(subreddit)
                .stream()
                .map(postMapper::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves posts created by a specific user.
     *
     * @param username The username whose posts should be fetched.
     * @return A list of posts created by the user.
     */
    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByUsername(String username) {
        MongoUserDocument user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found: " + username));

        return postRepository.findAllByUser(user)
                .stream()
                .map(postMapper::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Toggles the notification status for a post.
     *
     * @param id The post ID.
     * @param newStatus The new notification status (true/false).
     * @return The updated notification status.
     */
    @Transactional
    public boolean toggleNotificationStatus(String id, boolean newStatus) {
        MongoPostDocument post = postRepository.findById(id)
                .orElseThrow(() -> new CustomException("Post not found: " + id));

        post.setNotificationStatus(newStatus);
        postRepository.save(post);
        return newStatus;
    }

    /**
     * Updates an existing post after validating ownership and content safety.
     *
     * @param postRequest DTO containing the updated post details.
     * @return Updated post as DTO.
     */
    @Transactional
    public PostResponse update(MongoPostRequest postRequest) {
        String combinedText = postRequest.getPostName() + " " + postRequest.getDescription();

        // Moderate post text
        if (contentModerationService.isContentInappropriate(combinedText)) {
            throw new CustomException("Your post contains inappropriate content and cannot be saved.");
        }

        // Moderate attached media
        Photo photo = postRequest.getPhotoId() != null
                ? photoRepository.findById(postRequest.getPhotoId()).orElse(null)
                : null;
        if (photo != null && contentModerationService.isContentInappropriate(photo.getImage(), "image/jpeg")) {
            throw new CustomException("The uploaded image is inappropriate and cannot be saved.");
        }

        Video video = postRequest.getVideoId() != null
                ? videoRepository.findById(postRequest.getVideoId()).orElse(null)
                : null;
        if (video != null && contentModerationService.isContentInappropriate(video.getVideo(), "video/mp4")) {
            throw new CustomException("The uploaded video is inappropriate and cannot be saved.");
        }

        MongoUserDocument currentUser = authService.getCurrentUser();
        MongoPostDocument post = postRepository.findById(postRequest.getPostId())
                .orElseThrow(() -> new CustomException("Post not found with ID: " + postRequest.getPostId()));

        MongoSubredditDocument subreddit = subredditRepository.findByName(postRequest.getSubredditName())
                .orElseThrow(() -> new CustomException("Subreddit not found: " + postRequest.getSubredditName()));

        // Allow only admins or the post creator to edit
        if (currentUser.getRole().equals(Role.ADMIN) || post.getUser().equals(currentUser)) {
            post.setPostName(postRequest.getPostName());
            post.setDescription(postRequest.getDescription());
            post.setUrl(postRequest.getUrl());
            post.setSubreddit(subreddit);
            return postMapper.mapToDto(postRepository.save(post));
        } else {
            throw new CustomException("Insufficient privileges to edit this post!");
        }
    }

    /**
     * Deletes a post after validating ownership or admin privileges.
     *
     * @param id The post ID to delete.
     */
    @Transactional
    public void delete(String id) {
        log.info("Attempting to delete post with ID: {}", id);

        MongoUserDocument currentUser = authService.getCurrentUser();
        log.info("Current user attempting delete: {}", currentUser != null ? currentUser.getUsername() : "Anonymous");

        MongoPostDocument post = postRepository.findById(id)
                .orElseThrow(() -> new CustomException("Post not found with ID: " + id));

        // Only admin or owner can delete
        if (currentUser != null && (currentUser.getRole().equals(Role.ADMIN)
                || (post.getUser() != null && currentUser.equals(post.getUser())))) {
            log.info("User {} authorized to delete post {}", currentUser.getUsername(), id);
            postRepository.deleteById(id);
            log.info("Post {} deleted successfully.", id);
        } else {
            log.warn("Unauthorized delete attempt by {} on post {}",
                    currentUser != null ? currentUser.getUsername() : "Anonymous", id);
            throw new CustomException("Insufficient privileges to delete this post!");
        }
    }

    /**
     * Searches for posts by title using a case-insensitive query.
     *
     * @param query The search string.
     * @return A list of posts matching the query.
     */
    @Transactional(readOnly = true)
    public List<PostResponse> searchPosts(String query) {
        return postRepository.findByPostNameContainingIgnoreCase(query)
                .stream()
                .map(postMapper::mapToDto)
                .collect(Collectors.toList());
    }
}

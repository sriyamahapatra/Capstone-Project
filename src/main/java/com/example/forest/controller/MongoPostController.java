package com.example.forest.controller;

import com.example.forest.Exceptions.ValidationExceptions;
import com.example.forest.dto.MongoPostRequest;
import com.example.forest.dto.PostResponse;
import com.example.forest.service.MongoPostService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.ResponseEntity.status;

/**
 * MongoPostController.java
 *
 * Manages CRUD operations for posts stored in MongoDB.
 * Includes functionality for searching posts, toggling notifications,
 * and filtering posts by subreddit or username.
 *
 * Base endpoint: /api/v1/mongo/posts
 */
@RestController
@RequestMapping("/api/v1/mongo/posts")
@AllArgsConstructor
public class MongoPostController {

    private final MongoPostService mongoPostService;

    /**
     * Creates a new post.
     *
     * @param postRequest The post data from the client.
     * @param bindingResult Captures any validation errors from request body.
     * @return The created post along with HTTP 201 (Created) status.
     */
    @PostMapping({"", "/"})
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody MongoPostRequest postRequest,
                                                   BindingResult bindingResult) {

        // Validate incoming request body
        Optional<String> validationErrors = ValidationExceptions.processValidationErrors(bindingResult);
        validationErrors.ifPresent(System.out::print);

        // Delegate post creation to service layer
        return status(HttpStatus.CREATED).body(mongoPostService.save(postRequest));
    }

    /**
     * Toggles the notification setting for a post.
     *
     * @param id ID of the post to update.
     * @param newStatus The new notification status (true/false).
     * @return Updated notification status (true if enabled, false if disabled).
     */
    @PutMapping("/toggle-notifications/{id}")
    public ResponseEntity<Boolean> toggleNotifications(@PathVariable String id, @RequestBody boolean newStatus) {
        return status(HttpStatus.OK).body(mongoPostService.toggleNotificationStatus(id, newStatus));
    }

    /**
     * Retrieves all posts from the database.
     *
     * @return A list of all posts with HTTP 200 (OK).
     */
    @GetMapping({"", "/"})
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        return status(HttpStatus.OK).body(mongoPostService.getAllPosts());
    }

    /**
     * Retrieves a single post by its ID.
     *
     * @param id Unique ID of the post.
     * @return The post details or an appropriate error response.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable String id) {
        return status(HttpStatus.OK).body(mongoPostService.getPost(id));
    }

    /**
     * Retrieves posts belonging to a specific subreddit.
     *
     * @param subredditId The ID of the subreddit.
     * @return A list of posts associated with that subreddit.
     */
    @GetMapping("/subreddit-id/{subredditId}")
    public ResponseEntity<List<PostResponse>> getPostsBySubreddit(@PathVariable String subredditId) {
        return status(HttpStatus.OK).body(mongoPostService.getPostsBySubreddit(subredditId));
    }

    /**
     * Retrieves posts created by a specific user.
     *
     * @param username The username whose posts should be fetched.
     * @return A list of posts authored by the given user.
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<List<PostResponse>> getPostsByUsername(@PathVariable String username) {
        return status(HttpStatus.OK).body(mongoPostService.getPostsByUsername(username));
    }

    /**
     * Updates an existing post.
     * Requires the authenticated user to have either USER or ADMIN role.
     *
     * @param postRequest Updated post details.
     * @param bindingResult Captures any validation errors.
     * @return The updated post with HTTP 200 (OK).
     */
    @PutMapping({"", "/"})
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<PostResponse> updatePost(@Valid @RequestBody MongoPostRequest postRequest,
                                                   BindingResult bindingResult) {

        // Validate updated request data
        Optional<String> validationErrors = ValidationExceptions.processValidationErrors(bindingResult);
        validationErrors.ifPresent(System.out::print);

        // Perform the update operation
        return status(HttpStatus.OK).body(mongoPostService.update(postRequest));
    }

    /**
     * Deletes a post by its ID.
     *
     * @param id The ID of the post to delete.
     * @return HTTP 200 (OK) on successful deletion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable String id) {
        mongoPostService.delete(id);
        return new ResponseEntity<>(OK);
    }

    /**
     * Searches posts by text query (e.g., keyword, title, or content).
     *
     * @param query The search keyword or phrase.
     * @return A list of posts matching the search criteria.
     */
    @GetMapping("/search")
    public ResponseEntity<List<PostResponse>> searchPosts(@RequestParam("query") String query) {
        return ResponseEntity.ok(mongoPostService.searchPosts(query));
    }
}

package com.example.forest.controller;

import com.example.forest.dto.CommentsDto;
import com.example.forest.service.MongoCommentService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * CommentsController.java
 *
 * REST controller that manages comment-related operations in the Forest application.
 * Handles CRUD operations for comments using MongoDB via MongoCommentService.
 *
 * Base endpoint: /api/v1/comments
 */
@RestController
@RequestMapping("/api/v1/comments")
@AllArgsConstructor
public class CommentsController {

    private final MongoCommentService commentService;

    /**
     * Creates a new comment.
     *
     * @param commentsDto DTO containing comment details (post ID, content, username, etc.)
     * @return HTTP 201 (Created) on success
     */
    @PostMapping({"", "/"})
    public ResponseEntity<Void> createComment(@RequestBody CommentsDto commentsDto) {
        commentService.save(commentsDto);
        return new ResponseEntity<>(CREATED);
    }

    /**
     * Retrieves all comments from the database.
     *
     * @return A list of all comments with HTTP 200 (OK)
     */
    @GetMapping({"", "/"})
    public ResponseEntity<List<CommentsDto>> getAllComments() {
        return ResponseEntity.status(OK)
                .body(commentService.getAllComments());
    }

    /**
     * Retrieves all comments associated with a specific post.
     *
     * @param postId The ID of the post for which comments are requested
     * @return A list of comments related to the given post
     */
    @GetMapping("/post-id/{postId}")
    public ResponseEntity<List<CommentsDto>> getAllCommentsForPost(@PathVariable String postId) {
        return ResponseEntity.status(OK)
                .body(commentService.getAllCommentsForPost(postId));
    }

    /**
     * Retrieves all comments made by a specific user.
     *
     * @param userName The username whose comments are requested
     * @return A list of comments authored by the specified user
     */
    @GetMapping("/username/{userName}")
    public ResponseEntity<List<CommentsDto>> getAllCommentsForUser(@PathVariable String userName) {
        return ResponseEntity.status(OK)
                .body(commentService.getAllCommentsForUser(userName));
    }

    /**
     * Updates an existing comment.
     *
     * @param commentsDto DTO containing updated comment details
     * @return HTTP 200 (OK) on successful update
     */
    @PutMapping({"", "/"})
    public ResponseEntity<Void> updateComment(@RequestBody CommentsDto commentsDto) {
        commentService.update(commentsDto);
        return new ResponseEntity<>(OK);
    }

    /**
     * Deletes a comment.
     *
     * @param commentsDto DTO representing the comment to delete
     * @return HTTP 200 (OK) when the comment is successfully deleted
     */
    @DeleteMapping({"", "/"})
    public ResponseEntity<Void> deleteComment(@RequestBody CommentsDto commentsDto) {
        commentService.delete(commentsDto);
        return new ResponseEntity<>(OK);
    }
}

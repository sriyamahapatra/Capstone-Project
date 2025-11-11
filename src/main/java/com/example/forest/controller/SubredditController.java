package com.example.forest.controller;

import com.example.forest.dto.SubredditDto;
import com.example.forest.service.MongoSubredditService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.OK;

/**
 * SubredditController.java
 *
 * Handles CRUD operations for subreddits.
 * Allows users (and admins) to create, update, and delete subreddits,
 * while allowing everyone to view them.
 *
 * Base endpoint: /api/v1/subreddit
 */
@RestController
@RequestMapping("/api/v1/subreddit")
@AllArgsConstructor
@Slf4j
public class SubredditController {

    private final MongoSubredditService subredditService;

    /**
     * Creates a new subreddit.
     * Accessible only to users or admins.
     *
     * @param subredditDto The subreddit details provided in the request body.
     * @return The created subreddit object with HTTP 201 (Created).
     */
    @PostMapping({"", "/"})
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<SubredditDto> createSubreddit(@RequestBody SubredditDto subredditDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subredditService.save(subredditDto));
    }

    /**
     * Retrieves all subreddits.
     * Accessible publicly — no authentication required.
     *
     * @return A list of all available subreddits with HTTP 200 (OK).
     */
    @GetMapping({"", "/"})
    public ResponseEntity<List<SubredditDto>> getAllSubreddits() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(subredditService.getAll());
    }

    /**
     * Retrieves a specific subreddit by its ID.
     *
     * @param id The unique identifier of the subreddit.
     * @return The matching subreddit details with HTTP 200 (OK).
     */
    @GetMapping("/{id}")
    public ResponseEntity<SubredditDto> getSubreddit(@PathVariable String id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(subredditService.getSubreddit(id));
    }

    /**
     * Updates an existing subreddit.
     * Only users and admins can perform this operation.
     *
     * @param subredditDto The updated subreddit details.
     * @return The updated subreddit object with HTTP 201 (Created).
     */
    @PutMapping({"", "/"})
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<SubredditDto> updateSubreddit(@RequestBody SubredditDto subredditDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subredditService.update(subredditDto));
    }

    /**
     * Deletes a subreddit by its ID.
     * Accessible to users and admins only.
     *
     * @param id The unique identifier of the subreddit to be deleted.
     * @return HTTP 200 (OK) after successful deletion.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteSubreddit(@PathVariable String id) {
        subredditService.delete(id);
        return new ResponseEntity<>(OK);
    }
}

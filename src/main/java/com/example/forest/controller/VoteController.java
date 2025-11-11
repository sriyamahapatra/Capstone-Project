package com.example.forest.controller;

import com.example.forest.Exceptions.CustomException;
import com.example.forest.dto.VoteDto;
import com.example.forest.service.MongoVoteService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * VoteController.java
 *
 * Handles voting actions (upvotes/downvotes) on posts.
 * Delegates the voting logic to the {@link MongoVoteService}
 * and manages responses and exception handling for invalid actions.
 *
 * Base endpoint: /api/v1/votes
 */
@RestController
@RequestMapping("/api/v1/votes")
@AllArgsConstructor
public class VoteController {

    private final MongoVoteService voteService;

    /**
     * Handles a user's vote on a post (either upvote or downvote).
     *
     * @param voteDto Contains the post ID, vote type, and user details.
     * @return HTTP 200 (OK) on success, or 400 (Bad Request) if an invalid vote occurs.
     *
     * Flow:
     *  1. The request is passed to MongoVoteService for processing.
     *  2. If the vote is valid, it updates the post’s vote count.
     *  3. If a rule is violated (e.g., double vote), a CustomException is thrown and returned as 400.
     */
    @PostMapping({"", "/"})
    public ResponseEntity<String> vote(@RequestBody VoteDto voteDto) {
        try {
            voteService.vote(voteDto);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (CustomException ex) {
            // Return 400 with an error message if the vote action fails
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }
}

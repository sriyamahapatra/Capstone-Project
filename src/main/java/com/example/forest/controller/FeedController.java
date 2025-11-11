package com.example.forest.controller;

import com.example.forest.dto.PostResponse;
import com.example.forest.service.FeedService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * FeedController.java
 *
 * Handles requests related to the user's personalized feed.
 * Fetches posts based on the user's interests, subscriptions,
 * or recommendations from the FeedService.
 *
 * Base endpoint: /api/v1/feed
 */
@RestController
@RequestMapping("/api/v1/feed")
@AllArgsConstructor
public class FeedController {

    private final FeedService feedService;

    /**
     * Retrieves a list of posts for the authenticated user's personalized feed.
     *
     * @return List of {@link PostResponse} objects wrapped in HTTP 200 (OK)
     */
    @GetMapping
    public ResponseEntity<List<PostResponse>> getMyFeed() {
        // Delegate feed generation logic to the FeedService
        return ResponseEntity.ok(feedService.getMyFeed());
    }
}

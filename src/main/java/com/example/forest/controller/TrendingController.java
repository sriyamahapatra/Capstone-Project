package com.example.forest.controller;

import com.example.forest.dto.PostResponse;
import com.example.forest.service.TrendingService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * TrendingController.java
 *
 * Exposes an endpoint to retrieve trending posts based on
 * engagement metrics such as likes, comments, or views.
 *
 * Base endpoint: /api/v1/trending
 */
@RestController
@RequestMapping("/api/v1/trending")
@AllArgsConstructor
public class TrendingController {

    private final TrendingService trendingService;

    /**
     * Fetches a list of currently trending posts.
     *
     * @return A list of {@link PostResponse} objects wrapped in HTTP 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<PostResponse>> getTrendingPosts() {
        // Delegate trending post retrieval to the service layer
        return ResponseEntity.ok(trendingService.getTrendingPosts());
    }
}

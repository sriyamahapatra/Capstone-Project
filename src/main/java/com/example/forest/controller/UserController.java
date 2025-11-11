package com.example.forest.controller;

import com.example.forest.dto.PostResponse;
import com.example.forest.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * UserController.java
 *
 * Handles user-related operations.
 * Currently provides endpoints for retrieving personalized content such as
 * a user’s feed based on their subscriptions, interests, or activity.
 *
 * Base endpoint: /api/v1/users
 */
@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Retrieves the personalized feed for the authenticated user.
     *
     * @return A list of {@link PostResponse} objects representing
     *         posts tailored to the user's preferences.
     */
    @GetMapping("/feed")
    public ResponseEntity<List<PostResponse>> getMyFeed() {
        // Delegate feed retrieval logic to the UserService
        return ResponseEntity.ok(userService.getMyFeed());
    }
}

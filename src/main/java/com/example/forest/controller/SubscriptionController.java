package com.example.forest.controller;

import com.example.forest.service.SubscriptionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * SubscriptionController.java
 *
 * Handles subreddit subscription management for users.
 * Allows subscribing, unsubscribing, and fetching all subscribed subreddit names.
 *
 * Base endpoint: /api/v1/subscriptions
 */
@RestController
@RequestMapping("/api/v1/subscriptions")
@AllArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * Subscribes the current user to a subreddit.
     *
     * @param subredditName The name of the subreddit to subscribe to.
     * @return HTTP 200 (OK) after successful subscription.
     */
    @PostMapping("/subscribe/{subredditName}")
    public ResponseEntity<Void> subscribe(@PathVariable String subredditName) {
        subscriptionService.subscribe(subredditName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Unsubscribes the current user from a subreddit.
     *
     * @param subredditName The name of the subreddit to unsubscribe from.
     * @return HTTP 200 (OK) after successful unsubscription.
     */
    @PostMapping("/unsubscribe/{subredditName}")
    public ResponseEntity<Void> unsubscribe(@PathVariable String subredditName) {
        subscriptionService.unsubscribe(subredditName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Retrieves all subreddit names that the current user is subscribed to.
     *
     * @return A set of subreddit names wrapped in HTTP 200 (OK).
     */
    @GetMapping
    public ResponseEntity<Set<String>> getSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getSubscriptions());
    }
}

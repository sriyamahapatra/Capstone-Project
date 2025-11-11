package com.example.forest.service;

import com.example.forest.Exceptions.CustomException;
import com.example.forest.document.MongoSubredditDocument;
import com.example.forest.document.MongoUserDocument;
import com.example.forest.repository.mongodb.MongoSubredditRepository;
import com.example.forest.repository.mongodb.MongoUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

/**
 * Service responsible for handling subreddit subscriptions (follow/unfollow actions)
 * for authenticated users in the Forest application.
 *
 * <p>Subscriptions are managed via the user’s {@code interests} field,
 * which stores subreddit names representing joined communities.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SubscriptionService {

    private final MongoSubredditRepository subredditRepository;
    private final MongoUserRepository userRepository;
    private final AuthService authService;

    /**
     * Subscribes the current authenticated user to a given subreddit.
     *
     * @param subredditName The subreddit name to subscribe to.
     */
    public void subscribe(String subredditName) {
        if (subredditName == null || subredditName.isBlank()) {
            throw new CustomException("Subreddit name cannot be null or empty.");
        }

        MongoUserDocument user = authService.getCurrentUser();
        if (user == null) {
            throw new CustomException("User not authenticated. Please log in to subscribe.");
        }

        // Find subreddit (case-insensitive)
        MongoSubredditDocument subreddit = subredditRepository.findByNameIgnoreCase(subredditName)
                .orElseThrow(() -> new CustomException("Subreddit with name '" + subredditName + "' not found."));

        Set<String> interests = user.getInterests();
        if (interests == null) {
            throw new CustomException("User interests not initialized. Please try again later.");
        }

        boolean alreadySubscribed = interests.stream()
                .anyMatch(interest -> interest.equalsIgnoreCase(subreddit.getName()));

        if (alreadySubscribed) {
            log.info("⚠️ User '{}' already subscribed to '{}'.", user.getUsername(), subreddit.getName());
            return;
        }

        interests.add(subreddit.getName());
        userRepository.save(user);

        log.info("✅ User '{}' subscribed to subreddit '{}'.", user.getUsername(), subreddit.getName());
    }

    /**
     * Unsubscribes the current authenticated user from a given subreddit.
     *
     * @param subredditName The subreddit name to unsubscribe from.
     */
    public void unsubscribe(String subredditName) {
        if (subredditName == null || subredditName.isBlank()) {
            throw new CustomException("Subreddit name cannot be null or empty.");
        }

        MongoUserDocument user = authService.getCurrentUser();
        if (user == null) {
            throw new CustomException("User not authenticated. Please log in to unsubscribe.");
        }

        Set<String> interests = user.getInterests();
        if (interests == null || interests.isEmpty()) {
            log.info("⚠️ User '{}' has no subscriptions to remove.", user.getUsername());
            return;
        }

        Optional<String> interestToRemove = interests.stream()
                .filter(interest -> interest.equalsIgnoreCase(subredditName))
                .findFirst();

        if (interestToRemove.isPresent()) {
            interests.remove(interestToRemove.get());
            userRepository.save(user);
            log.info("🗑️ User '{}' unsubscribed from '{}'.", user.getUsername(), subredditName);
        } else {
            log.warn("⚠️ User '{}' attempted to unsubscribe from '{}' but was not subscribed.", user.getUsername(), subredditName);
        }
    }

    /**
     * Retrieves all subreddit subscriptions (user interests) for the current user.
     *
     * @return A {@link Set} of subreddit names.
     */
    @Transactional(readOnly = true)
    public Set<String> getSubscriptions() {
        MongoUserDocument user = authService.getCurrentUser();
        if (user == null) {
            throw new CustomException("User not authenticated. Please log in to view subscriptions.");
        }

        Set<String> interests = user.getInterests();
        if (interests == null || interests.isEmpty()) {
            log.info("ℹ️ User '{}' has no active subreddit subscriptions.", user.getUsername());
            return Set.of();
        }

        log.info("📬 Retrieved {} subscriptions for user '{}'.", interests.size(), user.getUsername());
        return interests;
    }
}

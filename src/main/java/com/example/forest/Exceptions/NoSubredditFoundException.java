package com.example.forest.Exceptions;

/**
 * NoSubredditFoundException.java
 *
 * Custom exception thrown when a requested subreddit cannot be found.
 * Extends {@link RuntimeException}, making it an unchecked exception —
 * meaning it doesn’t need to be declared or explicitly caught.
 *
 * This exception is commonly used when subreddit lookups by ID or name
 * return no matching results from the database.
 */
public class NoSubredditFoundException extends RuntimeException {

    /**
     * Constructs a new {@code NoSubredditFoundException} with a descriptive message.
     *
     * @param message the error message explaining why the exception was thrown.
     */
    public NoSubredditFoundException(String message) {
        super(message);
    }
}

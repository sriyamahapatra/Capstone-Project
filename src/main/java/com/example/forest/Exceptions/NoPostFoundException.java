package com.example.forest.Exceptions;

/**
 * NoPostFoundException.java
 *
 * Custom exception used to indicate that a requested post could not be found.
 * Extends {@link RuntimeException}, making it an unchecked exception — 
 * meaning it doesn't need to be declared or caught explicitly.
 *
 * This exception is typically thrown when a post ID or query does not
 * match any existing post in the database.
 */
public class NoPostFoundException extends RuntimeException {

    /**
     * Constructs a new {@code NoPostFoundException} with a detailed error message.
     *
     * @param message the error message describing why the exception occurred.
     */
    public NoPostFoundException(String message) {
        super(message);
    }
}

package com.example.forest.Exceptions;

/**
 * CustomException.java
 *
 * A custom exception class used for handling application-specific errors.
 * Extends {@link RuntimeException}, making it an unchecked exception — 
 * meaning it does not need to be declared in a method's `throws` clause.
 *
 * This class is typically used to wrap lower-level exceptions or
 * to represent domain-specific error scenarios in a cleaner, more readable way.
 */
public class CustomException extends RuntimeException {

    /**
     * Constructs a new {@code CustomException} with a detailed error message
     * and an underlying exception (cause).
     * <p>
     * Useful for wrapping checked exceptions while preserving the stack trace.
     *
     * @param exMessage  the detailed error message.
     * @param exception  the original exception being wrapped.
     */
    public CustomException(String exMessage, Exception exception) {
        super(exMessage, exception);
    }

    /**
     * Constructs a new {@code CustomException} with only a custom error message.
     * <p>
     * Use this constructor when no underlying exception needs to be propagated.
     *
     * @param exMessage  the detailed error message.
     */
    public CustomException(String exMessage) {
        super(exMessage);
    }
}

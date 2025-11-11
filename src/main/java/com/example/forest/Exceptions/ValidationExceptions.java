package com.example.forest.Exceptions;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Optional;

/**
 * ValidationExceptions.java
 *
 * Utility class that provides methods for processing and formatting
 * validation errors in incoming request payloads.
 * <p>
 * This class is not an exception itself — rather, it serves as a helper
 * to extract meaningful error messages from Spring's {@link BindingResult}
 * when input validation fails.
 */
public class ValidationExceptions {

    /**
     * Processes a {@link BindingResult} and returns a formatted error message
     * if validation errors exist. If no errors are found, an empty {@link Optional} is returned.
     *
     * @param bindingResult the result of validating a request body.
     * @return an {@link Optional} containing the formatted error message, or empty if there are no errors.
     */
    public static Optional<String> processValidationErrors(BindingResult bindingResult) {
        // Check if the validation result contains any errors
        if (bindingResult.hasErrors()) {
            // Build a readable error message
            StringBuilder errorMessage = new StringBuilder("Invalid request body. Errors: ");

            // Append each field and its corresponding validation error
            for (FieldError error : bindingResult.getFieldErrors()) {
                errorMessage
                        .append(error.getField())
                        .append(" - ")
                        .append(error.getDefaultMessage())
                        .append("; ");
            }

            // Return the complete error message if present
            if (!errorMessage.isEmpty()) {
                return Optional.of(errorMessage.toString());
            }
        }

        // Return empty if no validation errors exist
        return Optional.empty();
    }
}

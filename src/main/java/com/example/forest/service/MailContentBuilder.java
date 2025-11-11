package com.example.forest.service;

import org.thymeleaf.context.Context;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

/**
 * MailContentBuilder.java
 *
 * Service responsible for building dynamic HTML email content using Thymeleaf templates.
 * <p>
 * It replaces placeholders in the template with runtime data such as the recipient’s
 * username, email address, and a custom message (e.g., verification link, welcome message, etc.).
 */
@Service
@AllArgsConstructor
public class MailContentBuilder {

    /** Thymeleaf template engine used to process and render email templates. */
    private final TemplateEngine templateEngine;

    /**
     * Builds an email body by injecting dynamic content into the HTML template.
     *
     * @param message   the main message or body text (e.g., verification or password reset link).
     * @param username  the name of the recipient (used for personalization).
     * @param recipient the recipient’s email address (used within the template if needed).
     * @return a fully rendered HTML email as a {@link String}.
     */
    String build(String message, String username, String recipient) {
        Context context = new Context();
        context.setVariable("message", message);
        context.setVariable("username", username);
        context.setVariable("recipient", recipient);

        // Process and return the HTML output from the specified template
        return templateEngine.process("registrationConfirmationMailTemplate", context);
    }
}

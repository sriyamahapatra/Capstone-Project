package com.example.forest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * NotificationEmail.java
 *
 * Represents the structure of an email notification to be sent by the system.
 * This model is used to encapsulate the details required for sending
 * notification emails, such as user registration confirmation,
 * password reset, or post-related updates.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEmail {

    /** The subject line of the email. */
    private String subject;

    /** The recipient’s email address. */
    private String recipient;

    /** The username of the recipient (used for personalization). */
    private String username;

    /** The main content or body of the email. */
    private String body;
}

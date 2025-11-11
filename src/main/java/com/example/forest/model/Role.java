package com.example.forest.model;

/**
 * Role.java
 *
 * Defines the different roles available for users within the application.
 * Each role determines the level of access and permissions a user has
 * when interacting with the system.
 *
 * <p>Typical usage:</p>
 * <ul>
 *   <li><b>USER</b> — Standard user with basic privileges (can post, comment, vote).</li>
 *   <li><b>ADMIN</b> — User with full administrative privileges (can manage users, subreddits, and posts).</li>
 *   <li><b>MODERATOR</b> — User with elevated rights to manage specific subreddits or content.</li>
 * </ul>
 */
public enum Role {

    /** Standard user with basic access rights. */
    USER,

    /** Administrator with full application control. */
    ADMIN,

    /** Moderator with limited administrative privileges, usually subreddit-level. */
    MODERATOR
}

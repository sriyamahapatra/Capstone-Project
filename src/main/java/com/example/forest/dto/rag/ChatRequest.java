package com.example.forest.dto.rag;

import lombok.Data;

/**
 * ChatRequest.java
 *
 * Data Transfer Object (DTO) for sending a user query to the RAG (Retrieval-Augmented Generation) system.
 * Represents the payload received from the frontend when a user interacts with the chatbot.
 */
@Data // Lombok generates getters, setters, toString, equals, and hashCode methods
public class ChatRequest {

    /** The question or query input provided by the user. */
    private String question;

    /** Optional: Identifier for the user sending the query (useful for personalized context in future). */
    private String userId;
}

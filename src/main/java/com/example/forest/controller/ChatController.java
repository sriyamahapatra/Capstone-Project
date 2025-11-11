package com.example.forest.controller;

import com.example.forest.dto.rag.ChatRequest;
import com.example.forest.dto.rag.RAGQueryResponse;
import com.example.forest.service.RAGServiceClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * ChatController.java
 *
 * Handles chatbot interactions via a Retrieval-Augmented Generation (RAG) system.
 * Accepts user questions, forwards them to the RAG service,
 * and returns AI-generated answers asynchronously using Reactor (Mono).
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final RAGServiceClient ragServiceClient;

    /**
     * Injects the RAG service client dependency.
     * The service client communicates with the backend AI (RAG) API.
     */
    public ChatController(RAGServiceClient ragServiceClient) {
        this.ragServiceClient = ragServiceClient;
    }

    /**
     * POST endpoint for submitting a chat query to the RAG-powered chatbot.
     *
     * @param chatRequest Contains the user's input question
     * @return A Mono-wrapped ResponseEntity containing the chatbot's response
     *
     * Flow:
     *  1. Validate that the user question is not empty.
     *  2. Forward the question to the RAG service for processing.
     *  3. Return the AI-generated response or appropriate error status.
     */
    @PostMapping("/ask")
    public Mono<ResponseEntity<RAGQueryResponse>> askQuestion(@RequestBody ChatRequest chatRequest) {

        // Validate that the question field is not empty or blank
        if (chatRequest.getQuestion() == null || chatRequest.getQuestion().isBlank()) {
            return Mono.just(ResponseEntity.badRequest().build()); // Return 400 Bad Request
        }

        // Send the question to the RAG service asynchronously
        return ragServiceClient.queryRAGService(chatRequest.getQuestion())
                .map(response -> ResponseEntity.ok(response))            // Wrap valid responses in HTTP 200 OK
                .defaultIfEmpty(ResponseEntity.notFound().build());      // Return 404 if no response is available
    }
}

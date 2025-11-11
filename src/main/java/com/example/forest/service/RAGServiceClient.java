package com.example.forest.service;

import com.example.forest.dto.rag.RAGQueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * Service responsible for communicating with the external Python-based RAG (Retrieval-Augmented Generation) API.
 *
 * <p>This client sends user questions to a backend ML model endpoint (Python service) and
 * returns structured responses for downstream usage in the Forest application.</p>
 */
@Service
@Slf4j
public class RAGServiceClient {

    private final WebClient webClient;

    /**
     * Constructor-based injection for RAG service client.
     *
     * @param webClientBuilder Spring-managed WebClient builder.
     * @param ragServiceUrl    Base URL for the Python RAG microservice.
     */
    public RAGServiceClient(WebClient.Builder webClientBuilder,
                            @Value("${rag.service.url}") String ragServiceUrl) {
        this.webClient = webClientBuilder
                .baseUrl(ragServiceUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        log.info("✅ Initialized RAGServiceClient with base URL: {}", ragServiceUrl);
    }

    /**
     * Sends a user question to the Python RAG backend for contextual retrieval and generation.
     *
     * @param question The natural language question or query string.
     * @return A {@link Mono} stream containing the RAG model’s structured response.
     */
    public Mono<RAGQueryResponse> queryRAGService(String question) {
        if (question == null || question.isBlank()) {
            log.warn("⚠️ Attempted to query RAG service with empty question.");
            return Mono.error(new IllegalArgumentException("Question cannot be null or empty."));
        }

        RagApiRequest requestPayload = new RagApiRequest(question);
        log.debug("📤 Sending RAG query: {}", question);

        return this.webClient.post()
                .uri("/query")
                .bodyValue(requestPayload)
                .retrieve()
                .bodyToMono(RAGQueryResponse.class)
                .doOnNext(response ->
                        log.info("✅ Received RAG response for question '{}': {}", question, response.getAnswer()))
                .doOnError(WebClientResponseException.class, error ->
                        log.error("❌ RAG service returned error: {} - {}", error.getRawStatusCode(), error.getResponseBodyAsString()))
                .doOnError(error ->
                        log.error("🚨 Unexpected error calling RAG service: {}", error.getMessage()));
    }

    /**
     * Private inner DTO that matches the expected input structure of the Python RAG API.
     */
    private static class RagApiRequest {
        private final String question;

        public RagApiRequest(String question) {
            this.question = question;
        }

        public String getQuestion() {
            return question;
        }
    }
}

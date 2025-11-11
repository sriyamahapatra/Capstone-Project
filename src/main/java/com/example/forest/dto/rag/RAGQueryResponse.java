package com.example.forest.dto.rag;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * RAGQueryResponse.java
 *
 * Data Transfer Object (DTO) representing the response from the RAG (Retrieval-Augmented Generation) system.
 * Contains the generated answer and the supporting source documents that contributed to the response.
 */
@Data // Lombok automatically generates getters, setters, equals, hashCode, and toString
public class RAGQueryResponse {

    /** The AI-generated answer returned from the RAG model. */
    private String answer;

    /**
     * A list of source documents used by the RAG system to produce the answer.
     * Helps in maintaining transparency and explainability of AI-generated content.
     */
    @JsonProperty("source_documents")
    private List<SourceDocument> sourceDocuments;
}

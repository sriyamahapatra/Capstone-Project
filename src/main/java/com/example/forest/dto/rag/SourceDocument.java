package com.example.forest.dto.rag;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SourceDocument.java
 *
 * Represents an individual source document retrieved by the RAG (Retrieval-Augmented Generation) system.
 * Each source contributes to the generated answer and helps provide explainability for the AI response.
 */
@Data // Lombok automatically generates getters, setters, toString, equals, and hashCode
public class SourceDocument {

    /** The textual content extracted from the source document. */
    private String content;

    /** The similarity or relevance score assigned by the retrieval model. */
    private double score;

    /**
     * Unique identifier of the document in the knowledge base or vector store.
     * Serialized as "source_id" in JSON responses for API consistency.
     */
    @JsonProperty("source_id")
    private String sourceId;
}

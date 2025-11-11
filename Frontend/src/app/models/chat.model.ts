// src/app/models/chat.model.ts

export interface SourceDocument {
  content: string;
  score: number;
  sourceId: string;
}

export interface RAGQueryResponse {
  answer: string;
  // Make sure this is camelCase to match your Java DTOs,
  // assuming you used the @JsonProperty annotation or the global snake_case strategy.
  sourceDocuments: SourceDocument[];
}

export interface ChatRequest {
  question: string;
}

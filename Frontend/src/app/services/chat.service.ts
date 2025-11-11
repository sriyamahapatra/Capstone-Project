import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ChatRequest, RAGQueryResponse } from '../models/chat.model';

@Injectable({
  providedIn: 'root'
})
export class ChatService {

  // The URL of your Spring Boot backend endpoint
  private apiUrl = 'http://localhost:8080/api/chat/ask';

  constructor(private http: HttpClient) { }

  askQuestion(question: string): Observable<RAGQueryResponse> {
    const request: ChatRequest = {
      question: question
    };
    return this.http.post<RAGQueryResponse>(this.apiUrl, request);
  }
}

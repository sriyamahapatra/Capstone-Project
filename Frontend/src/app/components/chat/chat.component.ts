// src/app/components/chat/chat.component.ts

import { Component } from '@angular/core';
import { ChatService } from '../../services/chat.service';
import { SourceDocument } from '../../models/chat.model';
import { finalize } from 'rxjs/operators';

interface ChatMessage {
    sender: 'user' | 'bot';
    text: string;
    sourceDocuments?: SourceDocument[];
}

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent {
  // --- NEW PROPERTIES ---
  isOpen = false; // The widget is closed by default

  // --- EXISTING PROPERTIES ---
  messages: ChatMessage[] = [];
  currentQuestion = '';
  isLoading = false;
  error: string | null = null;

  constructor(private chatService: ChatService) {}

  // --- NEW METHOD ---
  toggleChat(): void {
    this.isOpen = !this.isOpen;
  }

  // --- MODIFIED METHOD ---
  sendMessage(): void {
    if (!this.currentQuestion.trim()) {
      return;
    }

    // If the user sends a message, make sure the window is open
    this.isOpen = true;

    this.messages.push({ sender: 'user', text: this.currentQuestion });
    const userQuestion = this.currentQuestion;
    this.currentQuestion = '';

    this.isLoading = true;
    this.error = null;

    this.chatService.askQuestion(userQuestion)
      .pipe(
        finalize(() => this.isLoading = false)
      )
      .subscribe({
        next: (response) => {
          this.messages.push({
            sender: 'bot',
            text: response.answer,
            sourceDocuments: response.sourceDocuments
          });
        },
        error: (err) => {
          console.error('API Error:', err);
          this.error = 'Sorry, something went wrong. Please try again.';
          this.messages.push({ sender: 'bot', text: this.error });
        }
      });
  }
}

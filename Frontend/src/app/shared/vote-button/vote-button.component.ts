import { Component, OnInit, Input, OnChanges, SimpleChanges } from '@angular/core';
import { PostModel } from 'src/app/shared/post-model';
import { VotePayload } from './vote-payload';
import { VoteType } from './vote-type';
import { VoteService } from '../vote.service';
import { AuthService } from 'src/app/auth/shared/auth.service';
import { PostService } from '../post.service';
import { throwError } from 'rxjs';
import { ToastrService } from 'ngx-toastr';
import { Router } from '@angular/router';

@Component({
  selector: 'app-vote-button',
  templateUrl: './vote-button.component.html',
  styleUrls: ['./vote-button.component.css']
})
export class VoteButtonComponent implements OnInit, OnChanges {

  @Input() post!: PostModel;
  @Input() compact: boolean = false; // New input for compact mode
  votePayload: VotePayload;
  isLoggedIn: boolean;
  isLoading: boolean = false;
  previousVoteCount: number = 0;

  constructor(
    private voteService: VoteService,
    private authService: AuthService,
    private postService: PostService,
    private toastr: ToastrService,
    private router: Router
  ) {
    this.votePayload = {
      voteType: undefined,
      postId: undefined
    };
    this.authService.loggedIn.subscribe((data: boolean) => this.isLoggedIn = data);
  }

  ngOnInit(): void {
    if (this.post) {
      this.previousVoteCount = this.post.voteCount;
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['post'] && changes['post'].currentValue) {
      this.previousVoteCount = this.post.voteCount;
    }
  }

  upvotePost(): void {
    if (!this.authService.isLoggedIn()) {
      this.toastr.error('You must be logged in to vote.');
      this.router.navigateByUrl('/login');
      return;
    }

    if (this.isLoading) return;

    // Toggle logic: if already upvoted, remove vote
    if (this.post.upVote) {
      this.votePayload.voteType = undefined;
    } else {
      this.votePayload.voteType = VoteType.UPVOTE;
      // If currently downvoted, we need to upvote (remove downvote and add upvote)
      if (this.post.downVote) {
        // This will handle the switch from downvote to upvote
      }
    }

    this.vote();
  }

  downvotePost(): void {
    if (!this.authService.isLoggedIn()) {
      this.toastr.error('You must be logged in to vote.');
      this.router.navigateByUrl('/login');
      return;
    }

    if (this.isLoading) return;

    // Toggle logic: if already downvoted, remove vote
    if (this.post.downVote) {
      this.votePayload.voteType = undefined;
    } else {
      this.votePayload.voteType = VoteType.DOWNVOTE;
      // If currently upvoted, we need to downvote (remove upvote and add downvote)
      if (this.post.upVote) {
        // This will handle the switch from upvote to downvote
      }
    }

    this.vote();
  }

  private vote(): void {
    if (!this.post?.id) {
      console.error('Post ID is undefined');
      return;
    }

    this.votePayload.postId = this.post.id;
    this.isLoading = true;

    this.voteService.vote(this.votePayload).subscribe({
      next: () => {
        this.updateVoteDetails();
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
        this.handleVoteError(error);
      }
    });
  }

  private updateVoteDetails(): void {
    if (!this.post?.id) {
      console.error('Post ID is undefined');
      return;
    }

    this.postService.getPost(this.post.id).subscribe({
      next: (post) => {
        this.previousVoteCount = this.post.voteCount;
        this.post = post;
        // You could add animation trigger here if needed
      },
      error: (error) => {
        console.error('Error updating vote details:', error);
        this.toastr.error('Error updating vote');
      }
    });
  }

  private handleVoteError(error: any): void {
    if (error.error?.message) {
      this.toastr.error(error.error.message);
    } else {
      this.toastr.error('An unexpected error occurred. Please try again.');
    }
    console.error('Vote error:', error);
  }
}

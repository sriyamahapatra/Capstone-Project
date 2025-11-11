import { Component, OnInit } from '@angular/core';
import { SubredditService } from 'src/app/subreddit/subreddit.service';
import { SubredditModel } from 'src/app/subreddit/subreddit-response';
import { AuthService } from '../../auth/shared/auth.service';

@Component({
  selector: 'app-subreddit-side-bar',
  templateUrl: './subreddit-side-bar.component.html',
  styleUrls: ['./subreddit-side-bar.component.css']
})
export class SubredditSideBarComponent implements OnInit {
  subreddits: Array<SubredditModel> = [];
  displayViewAll: boolean;

  constructor(private subredditService: SubredditService, private authService: AuthService) {
    this.subredditService.getAllSubreddits().subscribe(data => {
//       if (data.length > 3) {
//         this.subreddits = data.splice(0, 3);
//         this.displayViewAll = true;
//       } else {
        this.subreddits = data;
//       }
    });
  }

  ngOnInit(): void { }

  isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  isAuthor(subreddit: SubredditModel): boolean {
    return this.authService.getUserName() === subreddit.userName;
  }

  deleteSubreddit(id: string): void {
    this.subredditService.deleteSubreddit(id).subscribe(() => {
      this.subreddits = this.subreddits.filter(subreddit => subreddit.id !== id);
    });
  }
}

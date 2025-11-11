import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SubredditService } from '../subreddit.service';
import { PostService } from 'src/app/shared/post.service';
import { AuthService } from '../../auth/shared/auth.service';
import { faBell, faBellSlash } from '@fortawesome/free-solid-svg-icons';
import { faComments } from '@fortawesome/free-solid-svg-icons';
import { PostModel } from 'src/app/shared/post-model';

@Component({
  selector: 'app-view-subreddit',
  templateUrl: './view-subreddit.component.html',
  styleUrls: ['./view-subreddit.component.css']
})
export class ViewSubredditComponent implements OnInit {
  subredditId: string = '';
  selectedSubreddit: any;
  posts: PostModel[] = [];
  currentUser: string = '';

  faBell = faBell;
  faBellSlash = faBellSlash;
  faComments = faComments;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private subredditService: SubredditService,
    private postService: PostService
  ) { }

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.subredditId = params['id'];

      this.subredditService.getSubreddit(this.subredditId).subscribe(
        (data) => {
          this.selectedSubreddit = data;
        },
        (error) => {
          console.error('Error fetching subreddit:', error);
        }
      );

      this.postService.getPostsForSubreddit(this.subredditId).subscribe(
        (data) => {
          this.posts = data;
        },
        (error) => {
          console.error('Error fetching posts:', error);
        }
      );
    });

    if (this.authService.isLoggedIn()) {
      this.currentUser = this.authService.getUserName();
    }
  }

  toggleNotifications(postId: string, newStatus: boolean): void {
    this.postService.toggleNotificationsForPost(postId, newStatus).subscribe({
      next: (response: boolean) => {
        const postToUpdate = this.posts.find(post => post.id === postId);
        if (postToUpdate) {
          postToUpdate.notificationStatus = response;
        }
      },
      error: (error) => {
        console.error('Error toggling notifications:', error);
      }
    });
  }

  goToPost(postId: string): void {
    this.router.navigateByUrl('/view-post/' + postId);
  }

  editPost(id: string): void {
    this.router.navigateByUrl('/edit-post/' + id);
  }

  deletePost(id: string): void {
    this.postService.deletePost(id).subscribe(() => {
      this.posts = this.posts.filter(post => post.id !== id);
    });
  }

  isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  isAuthor(post: PostModel): boolean {
    return this.authService.getUserName() === post.userName;
  }

}

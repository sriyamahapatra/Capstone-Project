import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PostModel } from '../shared/post-model';
import { PostService } from '../shared/post.service';
import { AuthService } from '../auth/shared/auth.service';


@Component({
  selector: 'app-search-results',
  templateUrl: './search-results.component.html',
  styleUrls: ['./search-results.component.css'],
})
export class SearchResultsComponent implements OnInit {

  posts: PostModel[] = [];
  query: string = '';
  isLoading: boolean = true;
  hasSearched: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private postService: PostService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.query = decodeURIComponent(params['query']);
      this.fetchSearchResults(this.query);
    });
  }

  fetchSearchResults(query: string): void {
    this.isLoading = true;
    this.hasSearched = true;
    this.posts = [];

    if (!query) {
      this.isLoading = false;
      return;
    }

    this.postService.searchPosts(query).subscribe({
      next: (data) => {
        this.posts = data;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error fetching search results:', error);
        this.isLoading = false;
      }
    });
  }

  readPost(postId: string): void {
    this.router.navigateByUrl('/view-post/${postId}');
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

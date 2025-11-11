import { Component, OnInit, ViewEncapsulation, Input } from '@angular/core';
import { PostService } from '../post.service';
import { PostModel } from '../post-model';
import { faComments } from '@fortawesome/free-solid-svg-icons';
import { Router } from '@angular/router';
import { AuthService } from '../../auth/shared/auth.service';

@Component({
  selector: 'app-post-tile',
  templateUrl: './post-tile.component.html',
  styleUrls: ['./post-tile.component.css'],
  encapsulation: ViewEncapsulation.None,
})
export class PostTileComponent implements OnInit {

  faComments = faComments;
  @Input() posts: PostModel[];

  constructor(private router: Router, private postService: PostService, private authService: AuthService) { }

  ngOnInit(): void {
  }

  goToPost(id: string): void {
    this.router.navigateByUrl('/view-post/' + id);
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

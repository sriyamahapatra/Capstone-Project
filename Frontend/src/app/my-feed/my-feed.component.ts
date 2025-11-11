import { Component, OnInit } from '@angular/core';
import { PostModel } from '../shared/post-model';
import { UserService } from '../shared/user.service';

@Component({
  selector: 'app-my-feed',
  templateUrl: './my-feed.component.html',
  styleUrls: ['./my-feed.component.css']
})
export class MyFeedComponent implements OnInit {

  posts: Array<PostModel> = [];

  constructor(private userService: UserService) { }

  ngOnInit(): void {
    this.userService.getMyFeed().subscribe(posts => {
      this.posts = posts;
    });
  }

}

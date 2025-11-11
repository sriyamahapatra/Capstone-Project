import { Component, OnInit } from '@angular/core';
import { PostModel } from '../shared/post-model';
import { TrendingService } from '../shared/trending.service';

@Component({
  selector: 'app-trending',
  templateUrl: './trending.component.html',
  styleUrls: ['./trending.component.css']
})
export class TrendingComponent implements OnInit {

  posts: Array<PostModel> = [];

  constructor(private trendingService: TrendingService) { }

  ngOnInit(): void {
    this.trendingService.getTrendingPosts().subscribe(posts => {
      this.posts = posts;
    });
  }

}

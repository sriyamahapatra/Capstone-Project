import { Component, OnInit } from '@angular/core';
import { SubredditModel } from '../subreddit-response';
import { SubredditService } from '../subreddit.service';
import { throwError } from 'rxjs';
import { SubscriptionService } from '../subscription.service';

@Component({
  selector: 'app-list-subreddits',
  templateUrl: './list-subreddits.component.html',
  styleUrls: ['./list-subreddits.component.css']
})
export class ListSubredditsComponent implements OnInit {

  subreddits: Array<SubredditModel>;
  subscriptions: Array<string> = [];

  constructor(private subredditService: SubredditService, private subscriptionService: SubscriptionService) { }

  ngOnInit() {
    this.subredditService.getAllSubreddits().subscribe(data => {
      this.subreddits = data;
    }, error => {
      throwError(error);
    });
    this.getSubscriptions();
  }

  getSubscriptions() {
    this.subscriptionService.getSubscriptions().subscribe(data => {
      this.subscriptions = data;
    }, error => {
      throwError(error);
    });
  }

  isSubscribed(subredditName: string): boolean {
    return this.subscriptions.includes(subredditName);
  }

  subscribe(subredditName: string) {
    this.subscriptionService.subscribe(subredditName).subscribe(() => {
      this.getSubscriptions();
    }, error => {
      throwError(error);
    });
  }

  unsubscribe(subredditName: string) {
    this.subscriptionService.unsubscribe(subredditName).subscribe(() => {
      this.getSubscriptions();
    }, error => {
      throwError(error);
    });
  }
}

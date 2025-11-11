import { Component, OnInit } from '@angular/core';
import { faUser } from '@fortawesome/free-solid-svg-icons';
import { AuthService } from '../auth/shared/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-my-feed-header',
  templateUrl: './my-feed-header.component.html',
  styleUrls: ['./my-feed-header.component.css']
})
export class MyFeedHeaderComponent implements OnInit {
  faUser = faUser;
  isLoggedIn: boolean;
  username: string;
  searchTerm: string = '';

  constructor(public authService: AuthService, private router: Router) { }

  ngOnInit() {
    this.authService.loggedIn.subscribe((data: boolean) => this.isLoggedIn = data);
    this.authService.username.subscribe((data: string) => this.username = data);
    this.isLoggedIn = this.authService.isLoggedIn();
    this.username = this.authService.getUserName();
  }

  goToUserProfile() {
    this.router.navigateByUrl('/user-profile/' + this.username);
  }

  logout() {
    this.authService.logout();
    this.router.navigateByUrl('');
  }

  searchPosts() {
    if (this.searchTerm.trim() !== '') {
      this.router.navigateByUrl(`/search-results/${encodeURIComponent(this.searchTerm.trim())}`);
      this.searchTerm = ''; // Clear the search term after navigation
    }
  }
}

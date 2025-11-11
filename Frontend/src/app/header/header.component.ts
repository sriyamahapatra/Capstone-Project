import { Component, OnInit } from '@angular/core';
import { faUser } from '@fortawesome/free-solid-svg-icons';
import { AuthService } from '../auth/shared/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
  faUser = faUser;
  isLoggedIn: boolean;
  username: string;
  searchTerm: string = '';
  showCreateDropdown: boolean = false;
  showUserDropdown: boolean = false;
  createDropdownCloseTimer: any;
  userDropdownCloseTimer: any;

  constructor(private authService: AuthService, private router: Router) { }

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
    this.isLoggedIn = false;
    this.router.navigateByUrl('');
  }

  searchPosts() {
    if (this.searchTerm.trim() !== '') {
      this.router.navigateByUrl(`/search-results/${encodeURIComponent(this.searchTerm.trim())}`);
      this.searchTerm = ''; // Clear the search term after navigation
    }
  }

  // Create Dropdown Methods
  openCreateDropdown() {
    this.showCreateDropdown = true;
    this.clearCreateDropdownTimer();
  }

  closeCreateDropdown() {
    // Add a small delay before closing
    this.createDropdownCloseTimer = setTimeout(() => {
      this.showCreateDropdown = false;
    }, 500);
  }

  clearCreateDropdownTimer() {
    if (this.createDropdownCloseTimer) {
      clearTimeout(this.createDropdownCloseTimer);
    }
  }

  // User Dropdown Methods
  openUserDropdown() {
    this.showUserDropdown = true;
    this.clearUserDropdownTimer();
  }

  closeUserDropdown() {
    // Add a small delay before closing
    this.userDropdownCloseTimer = setTimeout(() => {
      this.showUserDropdown = false;
    }, 500);
  }

  clearUserDropdownTimer() {
    if (this.userDropdownCloseTimer) {
      clearTimeout(this.userDropdownCloseTimer);
    }
  }
}
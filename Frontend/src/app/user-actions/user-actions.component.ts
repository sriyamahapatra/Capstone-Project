import { Component, OnInit } from '@angular/core';
import { AuthService } from '../auth/shared/auth.service';

@Component({
  selector: 'app-user-actions',
  templateUrl: './user-actions.component.html',
  styleUrls: ['./user-actions.component.css']
})
export class UserActionsComponent implements OnInit {
  isLoggedIn: boolean;

  constructor(private authService: AuthService) { }

  ngOnInit(): void {
    this.authService.loggedIn.subscribe((data: boolean) => this.isLoggedIn = data);
    this.isLoggedIn = this.authService.isLoggedIn();
  }
}

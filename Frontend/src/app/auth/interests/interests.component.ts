import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../shared/auth.service';

@Component({
  selector: 'app-interests',
  templateUrl: './interests.component.html',
  styleUrls: ['./interests.component.css']
})
export class InterestsComponent implements OnInit {

  interests = ['Technology', 'Sports', 'Gaming', 'Music', 'Movies', 'Books', 'Travel', 'Food', 'Fashion', 'Art', 'Science', 'Health'];
  selectedInterests = new Set<string>();

  constructor(private authService: AuthService, private router: Router, private toastr: ToastrService) { }

  ngOnInit(): void { }

  toggleInterest(interest: string) {
    if (this.selectedInterests.has(interest)) {
      this.selectedInterests.delete(interest);
    } else {
      this.selectedInterests.add(interest);
    }
  }

  submitInterests() {
    this.authService.saveInterests(Array.from(this.selectedInterests))
      .subscribe(() => {
        this.toastr.success('Your interests have been saved.');
        this.router.navigate(['/']);
      }, error => {
        console.log(error);
        this.toastr.error('Failed to save interests. Please try again.');
      });
  }
}

import { Component, OnInit } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { SubredditModel } from '../subreddit-response';
import { Router } from '@angular/router';
import { SubredditService } from '../subreddit.service';
import { throwError } from 'rxjs';

@Component({
  selector: 'app-create-subreddit',
  templateUrl: './create-subreddit.component.html',
  styleUrls: ['./create-subreddit.component.css']
})
export class CreateSubredditComponent implements OnInit {
  createSubredditForm: FormGroup;
  subredditModel: SubredditModel;
  title = new FormControl('');
  description = new FormControl('');
  selectedFile: File | null = null;
  selectedImageUrl: string | null = null;

  constructor(private router: Router, private subredditService: SubredditService) {
    this.createSubredditForm = new FormGroup({
      title: new FormControl('', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(50)
      ]),
      description: new FormControl('', [
        Validators.required,
        Validators.minLength(10),
        Validators.maxLength(500)
      ])
    });
    this.subredditModel = {
      name: '',
      description: ''
    }
  }

  ngOnInit() {
    // Form status changes subscription for debugging
    this.createSubredditForm.statusChanges.subscribe(status => {
      console.log('Form status:', status);
    });
  }

  onFileSelected(event: any) {
    this.selectedFile = event.target.files[0];
    if (this.selectedFile) {
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.selectedImageUrl = e.target.result;
      };
      reader.readAsDataURL(this.selectedFile);
    }
  }

  discard() {
    if (this.createSubredditForm.dirty) {
      if (confirm('You have unsaved changes. Are you sure you want to discard them?')) {
        this.router.navigateByUrl('/');
      }
    } else {
      this.router.navigateByUrl('/');
    }
  }

  createSubreddit() {
    if (this.createSubredditForm.valid) {
      this.subredditModel.name = this.createSubredditForm.get('title')?.value;
      this.subredditModel.description = this.createSubredditForm.get('description')?.value;
      
      this.subredditService.createSubreddit(this.subredditModel).subscribe({
        next: (data) => {
          console.log('Topic created successfully:', data);
          this.router.navigateByUrl('/list-subreddits');
        },
        error: (error) => {
          console.error('Error creating topic:', error);
          // You can add user-friendly error handling here
          alert('Failed to create topic. Please try again.');
          throwError(error);
        }
      });
    } else {
      // Mark all fields as touched to show validation errors
      this.markFormGroupTouched(this.createSubredditForm);
    }
  }

  private markFormGroupTouched(formGroup: FormGroup) {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      if (control) {
        control.markAsTouched();
      }
    });
  }

  // Helper methods for template validation
  get titleControl() {
    return this.createSubredditForm.get('title');
  }

  get descriptionControl() {
    return this.createSubredditForm.get('description');
  }
}
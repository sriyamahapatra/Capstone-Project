// import { Component, OnInit } from '@angular/core';
// import { FormGroup, FormControl, Validators } from '@angular/forms';
// import { SubredditModel } from '../../subreddit/subreddit-response';
// import { Router, ActivatedRoute } from '@angular/router';
// import { PostService } from '../../shared/post.service';
// import { SubredditService } from '../../subreddit/subreddit.service';
// import { PostModel } from '../../shared/post-model';
// import { ToastrService } from 'ngx-toastr';
//
// @Component({
//   selector: 'app-edit-post',
//   templateUrl: './edit-post.component.html',
//   styleUrls: ['./edit-post.component.css']
// })
// export class EditPostComponent implements OnInit {
//
//   editPostForm: FormGroup;
//   post: PostModel;
//   subreddits: Array<SubredditModel>;
//   editorConfig = {
//     height: 500,
//     menubar: false,
//     plugins: [
//       'advlist autolink lists link image charmap print preview anchor',
//       'searchreplace visualblocks code fullscreen',
//       'insertdatetime media table paste code help wordcount'
//     ],
//     toolbar:
//       'undo redo | formatselect | bold italic backcolor | ' +
//       'alignleft aligncenter alignright alignjustify | ' +
//       'bullist numlist outdent indent | removeformat | help'
//   };
//
//   constructor(
//     private router: Router,
//     private postService: PostService,
//     private subredditService: SubredditService,
//     private route: ActivatedRoute,
//     private toastr: ToastrService
//   ) { }
//
//   ngOnInit(): void {
//     const postId = this.route.snapshot.params.id;
//     this.postService.getPost(postId).subscribe(post => {
//       this.post = post;
//       console.log('Fetched Post for editing:', this.post);
//       this.editPostForm = new FormGroup({
//         postName: new FormControl(this.post.postName, Validators.required),
//         subredditName: new FormControl(this.post.subredditName, Validators.required),
//         url: new FormControl(this.post.url, Validators.required),
//         description: new FormControl(this.post.description, Validators.required)
//       });
//     });
//
//     this.subredditService.getAllSubreddits().subscribe(subreddits => {
//       this.subreddits = subreddits;
//     });
//   }
//
//   editPost() {
//     if (this.editPostForm.invalid) {
//       this.toastr.error('Please fill in all required fields.');
//       return;
//     }
//
//     this.post.postName = this.editPostForm.get('postName').value;
//     this.post.subredditName = this.editPostForm.get('subredditName').value;
//     this.post.url = this.editPostForm.get('url').value;
//     this.post.description = this.editPostForm.get('description').value;
//
//     this.postService.updatePost(this.post).subscribe({
//       next: () => {
//         this.toastr.success('Post updated successfully!');
//         this.router.navigateByUrl('/view-post/' + this.post.id);
//       },
//       error: (err) => {
//         console.error('Update Post Error:', err);
//         let errorMessage = 'Error updating post. Please try again.';
//         if (err.error && typeof err.error === 'string') {
//           errorMessage = err.error;
//         } else if (err.message) {
//           errorMessage = err.message;
//         }
//         this.toastr.error(errorMessage);
//       }
//     });
//   }
//
//   discardPost() {
//     this.router.navigateByUrl('/view-post/' + this.post.id);
//   }
// }


import { Component, OnInit } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { SubredditModel } from '../../subreddit/subreddit-response';
import { Router, ActivatedRoute } from '@angular/router';
import { PostService } from '../../shared/post.service';
import { SubredditService } from '../../subreddit/subreddit.service';
import { PostModel } from '../../shared/post-model';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-edit-post',
  templateUrl: './edit-post.component.html',
  styleUrls: ['./edit-post.component.css']
})
export class EditPostComponent implements OnInit {

  editPostForm: FormGroup;
  post: PostModel;
  subreddits: Array<SubredditModel>;
  editorConfig = {
    height: 500,
    menubar: false,
    plugins: [
      'advlist autolink lists link image charmap print preview anchor',
      'searchreplace visualblocks code fullscreen',
      'insertdatetime media table paste code help wordcount'
    ],
    toolbar:
      'undo redo | formatselect | bold italic backcolor | ' +
      'alignleft aligncenter alignright alignjustify | ' +
      'bullist numlist outdent indent | removeformat | help'
  };

  constructor(
    private router: Router,
    private postService: PostService,
    private subredditService: SubredditService,
    private route: ActivatedRoute,
    private toastr: ToastrService
  ) { }

  ngOnInit(): void {
    this.editPostForm = new FormGroup({
        postName: new FormControl('', Validators.required),
        subredditName: new FormControl('', Validators.required),
        url: new FormControl('', Validators.required),
        description: new FormControl('', Validators.required)
      });
    const postId = this.route.snapshot.params.id;
    this.postService.getPost(postId).subscribe(post => {
      this.post = post;
      this.editPostForm.patchValue({
        postName: this.post.postName,
        subredditName: this.post.subredditName,
        url: this.post.url,
        description: this.post.description
      });
    });

    this.subredditService.getAllSubreddits().subscribe(subreddits => {
      this.subreddits = subreddits;
    });
  }

  editPost() {
    if (this.editPostForm.invalid) {
      this.toastr.error('Please fill in all required fields.');
      return;
    }

    this.post.postName = this.editPostForm.get('postName').value;
    this.post.subredditName = this.editPostForm.get('subredditName').value;
    this.post.url = this.editPostForm.get('url').value;
    this.post.description = this.editPostForm.get('description').value;

    this.postService.updatePost(this.post).subscribe({
      next: () => {
        this.toastr.success('Post updated successfully!');
        this.router.navigateByUrl('/view-post/' + this.post.id);
      },
      error: (err) => {
        console.error('Update Post Error:', err);
        let errorMessage = 'Error updating post. Please try again.';
        if (err.error && typeof err.error === 'string') {
          errorMessage = err.error;
        } else if (err.message) {
          errorMessage = err.message;
        }
        this.toastr.error(errorMessage);
      }
    });
  }

  discardPost() {
    this.router.navigateByUrl('/view-post/' + this.post.id);
  }
}

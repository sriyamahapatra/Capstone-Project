import { Component, OnInit } from '@angular/core';
import { FormGroup, Validators, FormControl } from '@angular/forms';
import { SubredditModel } from 'src/app/subreddit/subreddit-response';
import { Router } from '@angular/router';
import { PostService } from 'src/app/shared/post.service';
import { SubredditService } from 'src/app/subreddit/subreddit.service';
import { throwError } from 'rxjs';
import { CreatePostPayload } from './create-post.payload';
import { PhotoService } from 'src/app/shared/photo.service';
import { VideoService } from 'src/app/shared/video.service';
import { ToastrService } from 'ngx-toastr';

declare var tinymce: any; // Declare tinymce to avoid TypeScript errors

@Component({
  selector: 'app-create-post',
  templateUrl: './create-post.component.html',
  styleUrls: ['./create-post.component.css']
})
export class CreatePostComponent implements OnInit {

  createPostForm: FormGroup;
  postPayload: CreatePostPayload;
  subreddits: Array<SubredditModel>;
  editorConfig: any;

  constructor(
    private router: Router,
    private postService: PostService,
    private subredditService: SubredditService,
    private photoService: PhotoService,
    private videoService: VideoService,
    private toastr: ToastrService
  ) {
    this.postPayload = {
      postName: '',
      url: '',
      description: '',
      subredditName: '',
    };
  }

  ngOnInit() {
    this.editorConfig = {
      height: 500,
      menubar: false,
      plugins: [
        'advlist autolink lists link image charmap print preview anchor',
        'searchreplace visualblocks code fullscreen',
        'insertdatetime media table paste code help wordcount'
      ],
      toolbar:
        'undo redo | formatselect | bold italic backcolor | \
        alignleft aligncenter alignright alignjustify | \
        bullist numlist outdent indent | removeformat | image media | help',
      file_picker_callback: this.filePickerCallback.bind(this)
    };

    this.createPostForm = new FormGroup({
      postName: new FormControl('', Validators.required),
      subredditName: new FormControl('', Validators.required),
      url: new FormControl(''),
      description: new FormControl('', Validators.required),
    });

    this.subredditService.getAllSubreddits().subscribe((data) => {
      this.subreddits = data;
    }, error => {
      throwError(error);
    });
  }

  filePickerCallback(callback: any, value: any, meta: any) {
    const input = document.createElement('input');
    input.setAttribute('type', 'file');
    if (meta.filetype === 'image') {
      input.setAttribute('accept', 'image/*');
    } else if (meta.filetype === 'media') {
      input.setAttribute('accept', 'video/*');
    }

    input.onchange = () => {
      const file = input.files[0];

      if (meta.filetype === 'image') {
        this.photoService.upload(file).subscribe(response => {
          callback(response.url, { alt: file.name });
        });
      } else if (meta.filetype === 'media') {
        this.videoService.upload(file).subscribe(response => {
          callback(response.url, { source2: 'alt.mp4', poster: 'www.google.com/logos/google.jpg' });
        });
      }
    };

    input.click();
  }

  createPost() {
    this.postPayload.postName = this.createPostForm.get('postName').value;
    this.postPayload.subredditName = this.createPostForm.get('subredditName').value;
    this.postPayload.url = this.createPostForm.get('url').value;
    this.postPayload.description = this.createPostForm.get('description').value;

    this.postService.createPost(this.postPayload).subscribe(
      (data) => {
        this.router.navigateByUrl('/');
      },
      (error) => {
        // 🔹 Step 1: Ensure we never pass an object to toastr
        const safeError =
          typeof error === 'string'
            ? error
            : typeof error?.error === 'string'
              ? error.error
              : 'Your post violates our guidelines.';

        // 🔹 Step 2: Clean fixed message (no [object Object])
        const message = `
        <div style="
          display: flex;
          flex-direction: column;
          align-items: flex-start;
          font-family: 'Segoe UI', sans-serif;
          font-size: 14px;
          color: #fff;">

          <div style="display: flex; align-items: center; margin-bottom: 8px;">
            <span style="
              background-color: #ff4c4c;
              color: white;
              font-weight: bold;
              padding: 6px 10px;
              border-radius: 50%;
              margin-right: 8px;">

            </span>
            <strong>Post Rejected</strong>
          </div>

          <div style="
            background-color: rgba(255,255,255,0.1);
            border-left: 3px solid #ffaaaa;
            padding: 8px 12px;
            border-radius: 4px;
            width: 100%;">
            ${safeError}
          </div>
        </div>
      `;

        // 🔹 Step 3: Always provide empty title, to avoid showing [object Object]
        this.toastr.error(message, '', {
          enableHtml: true,
          closeButton: true,
          timeOut: 7000,
          extendedTimeOut: 3000,
          positionClass: 'toast-top-right',
          toastClass: 'ngx-toastr custom-toast-error'
        });
      }
    );
  }

  discardPost() {
    this.router.navigateByUrl('/');
  }
}

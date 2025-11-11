import { Component, OnInit } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { LoginRequestPayload } from './login-request.payload';
import { AuthService } from '../shared/auth.service';
import { Router, ActivatedRoute } from '@angular/router';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  loginForm: FormGroup;
  loginRequestPayload: LoginRequestPayload;
  registerSuccessMessage: string = '';
  isError: boolean = false;

  constructor(
    private authService: AuthService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private toastr: ToastrService
  ) {
    this.loginRequestPayload = {
      username: '',
      password: ''
    };
  }

  ngOnInit(): void {
    this.loginForm = new FormGroup({
      username: new FormControl('', Validators.required),
      password: new FormControl('', Validators.required)
    });

    this.activatedRoute.queryParams.subscribe(params => {
      if (params.registered !== undefined && params.registered === 'true') {
        this.toastr.success('Signup Successful');
        this.registerSuccessMessage =
          'Please check your inbox for an activation email and activate your account before logging in.';
      }
    });
  }

  login(): void {
    if (this.loginForm.invalid) {
      this.toastr.warning('Please fill in all required fields');
      return;
    }

    this.loginRequestPayload = this.loginForm.value;

    this.authService.login(this.loginRequestPayload).subscribe({
      next: () => {
        this.isError = false;
        this.toastr.success('Login Successful');
        this.router.navigateByUrl('/'); // Change '/home' to your desired route
      },
      error: (err) => {
        this.isError = true;
        this.toastr.error('Invalid username or password');
        console.error('Login error:', err);
      }
    });
  }
}

import { Component, OnInit } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { AuthService } from '../shared/auth.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent implements OnInit {

  forgotPasswordForm: FormGroup;

  constructor(private authService: AuthService, private toastr: ToastrService) { }

  ngOnInit(): void {
    this.forgotPasswordForm = new FormGroup({
      email: new FormControl('', [Validators.required, Validators.email])
    });
  }

  forgotPassword() {
    this.authService.forgotPassword(this.forgotPasswordForm.get('email').value)
      .subscribe(() => {
        this.toastr.success('Password reset link sent to your email.');
      }, error => {
        console.log(error);
        this.toastr.error('Failed to send password reset link. Please try again.');
      });
  }
}

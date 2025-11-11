import { Component, OnInit } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../shared/auth.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit {

  resetPasswordForm: FormGroup;
  token: string;
  passwordFieldType: string = 'password';

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private router: Router,
    private toastr: ToastrService
  ) { }

  ngOnInit(): void {
    this.token = this.route.snapshot.params['token'];
    this.resetPasswordForm = new FormGroup({
      password: new FormControl('', Validators.required),
      confirmPassword: new FormControl('', Validators.required)
    }, { validators: this.passwordsMatchValidator });
  }

  passwordsMatchValidator(form: FormGroup) {
    const password = form.get('password').value;
    const confirmPassword = form.get('confirmPassword').value;
    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  togglePasswordVisibility() {
    this.passwordFieldType = this.passwordFieldType === 'password' ? 'text' : 'password';
  }

  resetPassword() {
    if (this.resetPasswordForm.invalid) {
      return;
    }

    this.authService.resetPassword(this.token, this.resetPasswordForm.get('password').value)
      .subscribe(() => {
        this.toastr.success('Password reset successfully. You can now log in with your new password.');
        this.router.navigate(['/login']);
      }, error => {
        console.log(error);
        this.toastr.error('Failed to reset password. Please try again.');
      });
  }
}

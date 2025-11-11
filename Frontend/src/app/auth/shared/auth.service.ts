import { Injectable, Output, EventEmitter } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { SignupRequestPayload } from '../signup/singup-request.payload';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { LocalStorageService } from 'ngx-webstorage';
import { LoginRequestPayload } from '../login/login-request.payload';
import { LoginResponse } from '../login/login-response.payload';
import { map, tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private loggedInSubject = new BehaviorSubject<boolean>(this.isLoggedIn());
  private usernameSubject = new BehaviorSubject<string>(this.getUserName());

  loggedIn: Observable<boolean> = this.loggedInSubject.asObservable();
  username: Observable<string> = this.usernameSubject.asObservable();

  refreshTokenPayload = {
    refreshToken: this.getRefreshToken(),
    username: this.getUserName()
  }

  constructor(private httpClient: HttpClient,
    private localStorage: LocalStorageService) {
  }

  signup(signupRequestPayload: SignupRequestPayload): Observable<any> {
    return this.httpClient.post('http://localhost:8080/api/v1/auth/register', signupRequestPayload, { responseType: 'text' });
  }

  login(loginRequestPayload: LoginRequestPayload): Observable<boolean> {
    return this.httpClient.post<LoginResponse>('http://localhost:8080/api/v1/auth/login',
      loginRequestPayload).pipe(map(data => {
        this.localStorage.store('authenticationToken', data.authenticationToken);
        this.localStorage.store('username', data.username);
        this.localStorage.store('refreshToken', data.refreshToken);
        this.localStorage.store('expiresAt', data.expiresAt);

        this.loggedInSubject.next(true);
        this.usernameSubject.next(data.username);
        return true;
      }));
  }

  forgotPassword(email: string): Observable<any> {
    return this.httpClient.post('http://localhost:8080/api/v1/auth/forgot-password', email, { responseType: 'text' });
  }

  resetPassword(token: string, newPassword: string): Observable<any> {
    return this.httpClient.post('http://localhost:8080/api/v1/auth/reset-password?token=' + token, newPassword, { responseType: 'text' });
  }

  saveInterests(interests: string[]): Observable<any> {
    return this.httpClient.post('http://localhost:8080/api/v1/auth/interests', interests, { responseType: 'text' });
  }

  getJwtToken() {
    return this.localStorage.retrieve('authenticationToken');
  }

  refreshToken() {
    return this.httpClient.post<LoginResponse>('http://localhost:8080/api/v1/auth/refresh/token',
      this.refreshTokenPayload)
      .pipe(tap(response => {
        this.localStorage.clear('authenticationToken');
        this.localStorage.clear('expiresAt');

        this.localStorage.store('authenticationToken',
          response.authenticationToken);
        this.localStorage.store('expiresAt', response.expiresAt);
      }));
  }

  logout() {
    const refreshToken = this.getRefreshToken();
    if (refreshToken) {
      this.httpClient.post('http://localhost:8080/api/v1/auth/logout', { refreshToken: refreshToken },
        { responseType: 'text' })
        .subscribe(data => {
          console.log(data);
        }, error => {
          console.error('Logout failed on the backend', error);
        });
    }

    this.localStorage.clear('authenticationToken');
    this.localStorage.clear('username');
    this.localStorage.clear('refreshToken');
    this.localStorage.clear('expiresAt');

    this.loggedInSubject.next(false);
    this.usernameSubject.next(null);
  }

  getUserName() {
    return this.localStorage.retrieve('username');
  }
  getRefreshToken() {
    return this.localStorage.retrieve('refreshToken');
  }

  isLoggedIn(): boolean {
    return this.getJwtToken() != null;
  }

  isAdmin(): boolean {
    const user = this.getUserName();
    return user === 'admin';
  }
}

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {

  constructor(private http: HttpClient) { }

  subscribe(subredditName: string): Observable<any> {
    return this.http.post('http://localhost:8080/api/v1/subscriptions/subscribe/' + subredditName, {});
  }

  unsubscribe(subredditName: string): Observable<any> {
    return this.http.post('http://localhost:8080/api/v1/subscriptions/unsubscribe/' + subredditName, {});
  }

  getSubscriptions(): Observable<Array<string>> {
    return this.http.get<Array<string>>('http://localhost:8080/api/v1/subscriptions');
  }
}

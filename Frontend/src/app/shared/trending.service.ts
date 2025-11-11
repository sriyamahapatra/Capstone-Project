import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PostModel } from './post-model';

@Injectable({
  providedIn: 'root'
})
export class TrendingService {

  constructor(private http: HttpClient) { }

  getTrendingPosts(): Observable<Array<PostModel>> {
    return this.http.get<Array<PostModel>>('http://localhost:8080/api/v1/trending');
  }
}

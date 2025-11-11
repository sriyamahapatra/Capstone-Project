import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class VideoService {
  private baseUrl = 'http://localhost:8080/api/v1/videos/add';

  constructor(private http: HttpClient) { }

  upload(video: File): Observable<any> {
    const formData = new FormData();
    formData.append('title', video.name);
    formData.append('file', video);

    return this.http.post(this.baseUrl, formData);
  }
}

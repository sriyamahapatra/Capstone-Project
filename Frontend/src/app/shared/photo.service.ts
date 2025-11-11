import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PhotoService {
  private baseUrl = 'http://localhost:8080/api/v1/photos';

  constructor(private http: HttpClient) { }

  upload(image: File): Observable<any> {
    const formData = new FormData();
    formData.append('title', image.name);
    formData.append('image', image);

    return this.http.post(this.baseUrl, formData);
  }
}

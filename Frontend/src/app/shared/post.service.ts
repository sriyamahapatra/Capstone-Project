import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { PostModel } from './post-model';
import { Observable } from 'rxjs';
import { CreatePostPayload } from '../post/create-post/create-post.payload';
import { MongoPostRequest } from '../post/create-post/mongo-post-request.payload';

@Injectable({
  providedIn: 'root'
})
export class PostService {

  constructor(private http: HttpClient) { }

  getAllPosts(): Observable<Array<PostModel>> {
    return this.http.get<Array<PostModel>>('http://localhost:8080/api/v1/mongo/posts/');
  }

  createPost(postPayload: CreatePostPayload): Observable<any> {
    return this.http.post('http://localhost:8080/api/v1/mongo/posts/', postPayload);
  }

  getPost(id: string): Observable<PostModel> {
    return this.http.get<PostModel>('http://localhost:8080/api/v1/mongo/posts/' + id);
  }

  getAllPostsByUser(name: string): Observable<PostModel[]> {
    return this.http.get<PostModel[]>('http://localhost:8080/api/v1/mongo/posts/user/' + name);
  }

  getPostsForSubreddit(subredditId: string): Observable<PostModel[]> {
    return this.http.get<Array<PostModel>>('http://localhost:8080/api/v1/mongo/posts/subreddit-id/' + subredditId);
  }

  toggleNotificationsForPost(id: string, newStatus: boolean): Observable<boolean> {
    const url = `http://localhost:8080/api/v1/mongo/posts/toggle-notifications/${id}`;
    return this.http.put<boolean>(url, newStatus);
  }

  searchPosts(query: string): Observable<PostModel[]> {
    return this.http.get<PostModel[]>('http://localhost:8080/api/v1/mongo/posts/search?query=' + query);
  }

  deletePost(id: string): Observable<any> {
    return this.http.delete('http://localhost:8080/api/v1/mongo/posts/' + id);
  }

  updatePost(post: PostModel): Observable<any> {
    const mongoPostRequest: MongoPostRequest = {
      postId: post.id,
      subredditName: post.subredditName,
      postName: post.postName,
      url: post.url,
      description: post.description,
      photoId: null, // Assuming photoId is not updated via this form
      videoId: null  // Assuming videoId is not updated via this form
    };
    return this.http.put('http://localhost:8080/api/v1/mongo/posts/', mongoPostRequest);
  }
}

# Forest Project Report

## 1. Introduction

This document provides a detailed report on the Forest project, a web application with a Java backend and an Angular frontend.

## 2. Project Structure

### 2.1. Backend

The backend is a standard Maven project with the following structure:

```
.
├── .mvn
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── example
│   │   │           └── forest
│   │   │               ├── config
│   │   │               ├── controller
│   │   │               ├── document
│   │   │               ├── dto
│   │   │               ├── Exceptions
│   │   │               ├── mapper
│   │   │               ├── model
│   │   │               ├── repository
│   │   │               ├── security
│   │   │               └── service
│   │   └── resources
│   └── test
├── target
├── .gitattributes
├── .gitignore
├── Dockerfile
├── mvnw
├── mvnw.cmd
└── pom.xml
```

### 2.2. Frontend

The frontend is an Angular project with the following structure:

```
.
├── src
│   ├── app
│   ├── assets
│   └── environments
├── .editorconfig
├── .gitignore
├── angular.json
├── package.json
├── package-lock.json
├── README.md
├── tsconfig.app.json
├── tsconfig.json
└── tsconfig.spec.json
```

## 3. Backend Analysis

### 3.1. Dependencies (pom.xml)

The backend utilizes Spring Boot and a variety of libraries for different functionalities:

*   **Spring Boot Starters:** `spring-boot-starter-web`, `spring-boot-starter-security`, `spring-boot-starter-validation`, `spring-boot-starter-mail`, `spring-boot-starter-thymeleaf`, `spring-boot-starter-oauth2-resource-server`, `spring-boot-starter-data-mongodb`, `spring-boot-starter-actuator`, `spring-boot-starter-test`.
*   **Spring Security:** `spring-security-oauth2-jose`, `spring-security-test`.
*   **API Documentation:** `springdoc-openapi-starter-webmvc-ui` (Swagger UI).
*   **Utility Libraries:** `lombok` (boilerplate code reduction), `mapstruct` (object mapping), `timeago` (relative time formatting), `jetbrains.annotations`.
*   **Google Cloud:** `google-cloud-vertexai`, `google-auth-library-oauth2-http` (for Gemini API integration).
*   **File Utilities:** `commons-io`.
*   **Development Tools:** `spring-boot-devtools`.

### 3.2. Application Entry Point (ForestApplication.java)

The `ForestApplication.java` class serves as the main entry point for the Spring Boot application. It is annotated with `@SpringBootApplication`.

### 3.3. Application Configuration (application.properties)

The application's configuration is managed through `application.properties`. Key configurations include:

*   **Application Name:** `Forest`
*   **Server Port:** `8080`
*   **MongoDB Connection:** Configured to connect to a local MongoDB instance (`localhost:27017`) with the database named `forest`.
*   **JWT Configuration:**
    *   `jwt.expiration.time`: 900000 milliseconds (15 minutes).
*   **Email Configuration:** SMTP settings for sending emails.
*   **Logging:** `logging.level.com.example.forest.service: INFO` for service-level logging.

### 3.4. Controllers

The `controller` package contains the REST API endpoints for the application.

#### 3.4.1. AuthController

Handles user authentication and authorization flows:

*   `POST /api/v1/auth/register`: Registers a new user.
*   `GET /api/v1/auth/accountVerification/{token}`: Verifies a user's account.
*   `POST /api/v1/auth/login`: Authenticates a user and returns JWT and refresh tokens.
*   `POST /api/v1/auth/refresh/token`: Refreshes an expired JWT.
*   `POST /api/v1/auth/logout`: Invalidates a refresh token.
*   `POST /api/v1/auth/forgot-password`: Initiates the password reset process.
*   `POST /api/v1/auth/reset-password`: Resets the user's password.
*   `POST /api/v1/auth/interests`: Saves user interests.

#### 3.4.2. ChatController

*   `POST /api/chat/ask`: Submits a chat query to the RAG-powered chatbot.

#### 3.4.3. FeedController

*   `GET /api/v1/feed`: Retrieves a list of posts for the authenticated user's personalized feed.

#### 3.4.4. UserController

*   `GET /api/v1/users/feed`: Retrieves the personalized feed for the authenticated user.

#### 3.4.5. VoteController

*   `POST /api/v1/votes`: Handles a user's vote on a post.

#### 3.4.6. PhotoController

*   `POST /api/v1/photos`: Uploads a new photo.
*   `GET /api/v1/photos/{id}`: Retrieves a stored photo by its ID.

#### 3.4.7. VideoController

*   `POST /api/v1/videos/add`: Uploads a new video file.
*   `GET /api/v1/videos/{id}`: Retrieves metadata for a specific video.
*   `GET /api/v1/videos/stream/{id}`: Streams a stored video.

#### 3.4.8. CommentsController

*   `POST /api/v1/comments`: Creates a new comment.
*   `GET /api/v1/comments`: Retrieves all comments.
*   `GET /api/v1/comments/post-id/{postId}`: Retrieves all comments for a specific post.
*   `GET /api/v1/comments/username/{userName}`: Retrieves all comments made by a specific user.
*   `PUT /api/v1/comments`: Updates an existing comment.
*   `DELETE /api/v1/comments`: Deletes a comment.

#### 3.4.9. TrendingController

*   `GET /api/v1/trending`: Fetches a list of currently trending posts.

#### 3.4.10. MongoPostController

*   `POST /api/v1/mongo/posts`: Creates a new post.
*   `PUT /api/v1/mongo/posts/toggle-notifications/{id}`: Toggles the notification setting for a post.
*   `GET /api/v1/mongo/posts`: Retrieves all posts.
*   `GET /api/v1/mongo/posts/{id}`: Retrieves a single post by its ID.
*   `GET /api/v1/mongo/posts/subreddit-id/{subredditId}`: Retrieves posts belonging to a specific subreddit.
*   `GET /api/v1/mongo/posts/user/{username}`: Retrieves posts created by a specific user.
*   `PUT /api/v1/mongo/posts`: Updates an existing post.
*   `DELETE /api/v1/mongo/posts/{id}`: Deletes a post by its ID.
*   `GET /api/v1/mongo/posts/search`: Searches posts by text query.

#### 3.4.11. SubredditController

*   `POST /api/v1/subreddit`: Creates a new subreddit.
*   `GET /api/v1/subreddit`: Retrieves all subreddits.
*   `GET /api/v1/subreddit/{id}`: Retrieves a specific subreddit by its ID.
*   `PUT /api/v1/subreddit`: Updates an existing subreddit.
*   `DELETE /api/v1/subreddit/{id}`: Deletes a subreddit by its ID.

#### 3.4.12. SubscriptionController

*   `POST /api/v1/subscriptions/subscribe/{subredditName}`: Subscribes the current user to a subreddit.
*   `POST /api/v1/subscriptions/unsubscribe/{subredditName}`: Unsubscribes the current user from a subreddit.
*   `GET /api/v1/subscriptions`: Retrieves all subreddit names that the current user is subscribed to.

## 4. Frontend Analysis

### 4.1. Dependencies (package.json)

The frontend is built with Angular and utilizes several libraries:

*   **Angular:** `@angular/core`, `@angular/common`, `@angular/forms`, `@angular/router`, etc.
*   **UI Components:** `@ng-bootstrap/ng-bootstrap` (Bootstrap components), `@fortawesome/angular-fontawesome` (Font Awesome icons).
*   **Rich Text Editor:** `@tinymce/tinymce-angular`.
*   **Notifications:** `ngx-toastr`.
*   **Local Storage:** `ngx-webstorage`.

### 4.2. Application Module (app.module.ts)

The `AppModule` is the root module of the application. It imports and declares all the necessary components, modules, and providers.

### 4.3. Application Routing (app-routing.module.ts)

The `AppRoutingModule` defines the application's routes:

*   **Public Routes:**
    *   `/`: `HomeComponent`
    *   `/trending`: `TrendingComponent`
    *   `/view-subreddit/:id`: `ViewSubredditComponent`
    *   `/view-post/:id`: `ViewPostComponent`
    *   `/list-subreddits`: `ListSubredditsComponent`
    *   `/signup`: `SignupComponent`
    *   `/login`: `LoginComponent`
    *   `/search-results/:query`: `SearchResultsComponent`
    *   `/guidelines`: `GuidelinesComponent`
    *   `/forgot-password`: `ForgotPasswordComponent`
    *   `/reset-password/:token`: `ResetPasswordComponent`
*   **Protected Routes (requiring authentication via `AuthGuard`):**
    *   `/my-feed`: `MyFeedComponent`
    *   `/user-profile/:name`: `UserProfileComponent`
    *   `/create-post`: `CreatePostComponent`
    *   `/create-subreddit`: `CreateSubredditComponent`
    *   `/edit-post/:id`: `EditPostComponent`
    *   `/interests`: `InterestsComponent`

### 4.4. Key Components and Services

The frontend is organized into several feature modules, each with its own components and services for features like authentication, posts, subreddits, and comments.

## 5. Conclusion

The Forest project is a well-structured full-stack web application. The backend is a robust Spring Boot application with a clear separation of concerns, a comprehensive REST API, and a solid security implementation. The frontend is a modern Angular application with a modular architecture and a good user experience. The project demonstrates a good understanding of modern web development practices.

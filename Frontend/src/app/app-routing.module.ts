import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { SignupComponent } from './auth/signup/signup.component';
import { LoginComponent } from './auth/login/login.component';
import { HomeComponent } from './home/home.component';
import { CreatePostComponent } from './post/create-post/create-post.component';
import { CreateSubredditComponent } from './subreddit/create-subreddit/create-subreddit.component';
import { ListSubredditsComponent } from './subreddit/list-subreddits/list-subreddits.component';
import { ViewPostComponent } from './post/view-post/view-post.component';
import { UserProfileComponent } from './auth/user-profile/user-profile.component';
import { AuthGuard } from './auth/auth.guard';
import { ViewSubredditComponent } from './subreddit/view-subreddit/view-subreddit.component';
import { SearchResultsComponent } from './search-results/search-results.component';
import { GuidelinesComponent } from './guidelines/guidelines.component';
import { ForgotPasswordComponent } from './auth/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './auth/reset-password/reset-password.component';
import { InterestsComponent } from './auth/interests/interests.component';
import { EditPostComponent } from './post/edit-post/edit-post.component';
import { MyFeedComponent } from './my-feed/my-feed.component';
import { TrendingComponent } from './trending/trending.component';

const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'my-feed', component: MyFeedComponent, canActivate: [AuthGuard] },
  { path: 'trending', component: TrendingComponent },
  { path: 'view-subreddit/:id', component: ViewSubredditComponent },
  { path: 'view-post/:id', component: ViewPostComponent },
  { path: 'user-profile/:name', component: UserProfileComponent, canActivate: [AuthGuard] },
  { path: 'list-subreddits', component: ListSubredditsComponent },
  { path: 'create-post', component: CreatePostComponent, canActivate: [AuthGuard] },
  { path: 'create-subreddit', component: CreateSubredditComponent, canActivate: [AuthGuard] },
  { path: 'edit-post/:id', component: EditPostComponent, canActivate: [AuthGuard] },
  { path: 'signup', component: SignupComponent },
  { path: 'search-results/:query', component: SearchResultsComponent },
  { path: 'login', component: LoginComponent },
  { path: 'guidelines', component: GuidelinesComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'reset-password/:token', component: ResetPasswordComponent },
  { path: 'interests', component: InterestsComponent, canActivate: [AuthGuard] }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

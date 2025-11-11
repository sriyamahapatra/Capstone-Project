import { ComponentFixture, TestBed } from '@angular/core/testing';
import { VoteButtonComponent } from './vote-button.component';
import { VoteService } from '../vote.service';
import { AuthService } from 'src/app/auth/shared/auth.service';
import { PostService } from '../post.service';
import { ToastrService } from 'ngx-toastr';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { PostModel } from 'src/app/shared/post-model'; // Add this import

describe('VoteButtonComponent', () => {
  let component: VoteButtonComponent;
  let fixture: ComponentFixture<VoteButtonComponent>;
  let mockVoteService: jasmine.SpyObj<VoteService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockPostService: jasmine.SpyObj<PostService>;
  let mockToastr: jasmine.SpyObj<ToastrService>;
  let mockRouter: jasmine.SpyObj<Router>;

  const mockPost: PostModel = {
    id:"1",
    postName: 'Test Post',
    url: 'http://test.com',
    description: 'Test description',
    voteCount: 10,
    userName: 'testuser',
    subredditName: 'testsub',
    commentCount: 5,
    duration: '2 hours ago',
    upVote: false,
    downVote: false,
    notificationStatus:false,
  };

  beforeEach(async () => {
    const voteServiceSpy = jasmine.createSpyObj('VoteService', ['vote']);
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['isLoggedIn'], { loggedIn: of(true) });
    const postServiceSpy = jasmine.createSpyObj('PostService', ['getPost']);
    const toastrSpy = jasmine.createSpyObj('ToastrService', ['error', 'success']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigateByUrl']);

    await TestBed.configureTestingModule({
      declarations: [VoteButtonComponent],
      providers: [
        { provide: VoteService, useValue: voteServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: PostService, useValue: postServiceSpy },
        { provide: ToastrService, useValue: toastrSpy },
        { provide: Router, useValue: routerSpy }
      ]
    }).compileComponents();

    mockVoteService = TestBed.inject(VoteService) as jasmine.SpyObj<VoteService>;
    mockAuthService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    mockPostService = TestBed.inject(PostService) as jasmine.SpyObj<PostService>;
    mockToastr = TestBed.inject(ToastrService) as jasmine.SpyObj<ToastrService>;
    mockRouter = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VoteButtonComponent);
    component = fixture.componentInstance;
    component.post = mockPost;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should upvote post when logged in', () => {
    mockAuthService.isLoggedIn.and.returnValue(true);
    mockVoteService.vote.and.returnValue(of({}));
    mockPostService.getPost.and.returnValue(of(mockPost));

    component.upvotePost();

    expect(mockVoteService.vote).toHaveBeenCalled();
  });

  it('should redirect to login when not logged in', () => {
    mockAuthService.isLoggedIn.and.returnValue(false);

    component.upvotePost();

    expect(mockToastr.error).toHaveBeenCalledWith('You must be logged in to vote.');
    expect(mockRouter.navigateByUrl).toHaveBeenCalledWith('/login');
  });
});

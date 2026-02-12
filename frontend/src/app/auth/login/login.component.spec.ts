import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { AuthService } from '../../core/services/auth.service';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';

describe('LoginComponent (Jest)', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authService: jest.Mocked<AuthService>;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        LoginComponent,                 // ✅ standalone component
        RouterTestingModule.withRoutes([])
      ],
      providers: [
        {
          provide: AuthService,
          useValue: {
            login: jest.fn()
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;

    authService = TestBed.inject(AuthService) as jest.Mocked<AuthService>;
    router = TestBed.inject(Router);

    // ✅ mock browser APIs
    jest.spyOn(window, 'alert').mockImplementation(() => {});
    jest.spyOn(router, 'navigate').mockResolvedValue(true);
    jest.spyOn(Storage.prototype, 'setItem');

    fixture.detectChanges();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  // ---------------------------------------------------
  // BASIC
  // ---------------------------------------------------
  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  // ---------------------------------------------------
  // ADMIN LOGIN
  // ---------------------------------------------------
  it('should login and navigate to admin dashboard for ADMIN role', () => {
    authService.login.mockReturnValue(
      of({
        role: 'ADMIN',
        email: 'admin@test.com'
      } as any)
    );

    component.email = 'admin@test.com';
    component.password = 'password';
    component.login();

    expect(authService.login).toHaveBeenCalledWith({
      email: 'admin@test.com',
      password: 'password'
    });

    expect(localStorage.setItem).toHaveBeenCalledWith(
      'loggedInUser',
      expect.any(String)
    );

    expect(router.navigate).toHaveBeenCalledWith(['/admin-dashboard']);
  });

  // ---------------------------------------------------
  // EMPLOYEE LOGIN
  // ---------------------------------------------------
  it('should login and navigate to employee dashboard for EMPLOYEE role', () => {
    authService.login.mockReturnValue(
      of({
        role: 'EMPLOYEE',
        email: 'emp@test.com'
      } as any)
    );

    component.login();

    expect(router.navigate).toHaveBeenCalledWith(['/employee-dashboard']);
  });

  // ---------------------------------------------------
  // UNKNOWN ROLE
  // ---------------------------------------------------
  it('should show alert for unknown role', () => {
    authService.login.mockReturnValue(
      of({
        role: 'MANAGER'
      } as any)
    );

    component.login();

    expect(window.alert).toHaveBeenCalledWith('Unknown role');
    expect(router.navigate).not.toHaveBeenCalled();
  });

  // ---------------------------------------------------
  // LOGIN FAILURE
  // ---------------------------------------------------
  it('should show error alert on login failure', () => {
    authService.login.mockReturnValue(
      throwError(() => new Error('Invalid credentials'))
    );

    component.login();

    expect(window.alert).toHaveBeenCalledWith('Invalid email or password');
  });
});

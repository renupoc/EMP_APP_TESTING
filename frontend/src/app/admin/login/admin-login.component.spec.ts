import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdminLoginComponent } from './admin-login.component';
import { AuthService } from '../../core/services/auth.service';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';

describe('AdminLoginComponent (Jest)', () => {
  let component: AdminLoginComponent;
  let fixture: ComponentFixture<AdminLoginComponent>;
  let authService: jest.Mocked<AuthService>;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        AdminLoginComponent, // standalone component
        RouterTestingModule.withRoutes([]) // ✅ fixes ActivatedRoute
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

    fixture = TestBed.createComponent(AdminLoginComponent);
    component = fixture.componentInstance;

    authService = TestBed.inject(AuthService) as jest.Mocked<AuthService>;
    router = TestBed.inject(Router);

    // ✅ mock alert globally
    jest.spyOn(window, 'alert').mockImplementation(() => {});
    jest.spyOn(router, 'navigate').mockResolvedValue(true);

    // ✅ mock localStorage correctly
    jest.spyOn(Storage.prototype, 'setItem');

    fixture.detectChanges();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should login successfully and navigate for ADMIN role', () => {
    authService.login.mockReturnValue(
      of({
        role: 'ADMIN',
        employeeId: 10,
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
    expect(localStorage.setItem).toHaveBeenCalledWith('adminId', '10');
    expect(router.navigate).toHaveBeenCalledWith(['/admin-dashboard']);
  });

  it('should block login if role is not ADMIN', () => {
    authService.login.mockReturnValue(
      of({
        role: 'EMPLOYEE',
        employeeId: 5
      } as any)
    );

    component.login();

    expect(window.alert).toHaveBeenCalledWith('Access denied. Admin only.');
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('should show error alert on login failure', () => {
    authService.login.mockReturnValue(
      throwError(() => new Error('Invalid credentials'))
    );

    component.login();

    expect(window.alert).toHaveBeenCalledWith('Invalid email or password');
  });
});

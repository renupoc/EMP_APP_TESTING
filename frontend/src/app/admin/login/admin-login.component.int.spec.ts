import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdminLoginComponent } from './admin-login.component';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Router } from '@angular/router';

describe('AdminLoginComponent – Integration Test', () => {
  let fixture: ComponentFixture<AdminLoginComponent>;
  let component: AdminLoginComponent;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        AdminLoginComponent,               // ✅ standalone component
        HttpClientTestingModule,           // ✅ real HttpClient (mocked backend)
        RouterTestingModule.withRoutes([]) // ✅ router integration
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminLoginComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);

    // ✅ mock browser APIs
    jest.spyOn(window, 'alert').mockImplementation(() => {});
    jest.spyOn(router, 'navigate').mockResolvedValue(true);
    jest.spyOn(Storage.prototype, 'setItem');

    fixture.detectChanges(); // render template
  });

  afterEach(() => {
    httpMock.verify();
    jest.clearAllMocks();
    localStorage.clear();
  });

  // ---------------------------------------------------
  // SUCCESS – ADMIN LOGIN
  // ---------------------------------------------------
  it('should login admin and navigate to admin dashboard', () => {
    component.email = 'admin@test.com';
    component.password = 'admin123';
    fixture.detectChanges();

    const loginButton = fixture.nativeElement.querySelector(
      'button[type="submit"]'
    ) as HTMLButtonElement;

    loginButton.click();

    const req = httpMock.expectOne(
      'http://localhost:8080/api/auth/login'
    );

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      email: 'admin@test.com',
      password: 'admin123'
    });

    req.flush({
      role: 'ADMIN',
      employeeId: 101,
      email: 'admin@test.com'
    });

    expect(localStorage.getItem('loggedInUser')).toContain('ADMIN');
    expect(localStorage.getItem('adminId')).toBe('101');
    expect(router.navigate).toHaveBeenCalledWith(['/admin-dashboard']);
  });

  // ---------------------------------------------------
  // FAILURE – NON-ADMIN ROLE
  // ---------------------------------------------------
  it('should block login if role is not ADMIN', () => {
    component.login();

    const req = httpMock.expectOne(
      'http://localhost:8080/api/auth/login'
    );

    req.flush({
      role: 'EMPLOYEE',
      employeeId: 201
    });

    expect(window.alert).toHaveBeenCalledWith(
      'Access denied. Admin only.'
    );
    expect(router.navigate).not.toHaveBeenCalled();
  });

  // ---------------------------------------------------
  // FAILURE – INVALID CREDENTIALS
  // ---------------------------------------------------
  it('should show error alert on login failure', () => {
    component.login();

    const req = httpMock.expectOne(
      'http://localhost:8080/api/auth/login'
    );

    req.flush(
      { message: 'Invalid credentials' },
      { status: 401, statusText: 'Unauthorized' }
    );

    expect(window.alert).toHaveBeenCalledWith(
      'Invalid email or password'
    );
  });
});

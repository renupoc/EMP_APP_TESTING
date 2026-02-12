import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Router } from '@angular/router';

describe('LoginComponent – Integration Test', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let component: LoginComponent;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        LoginComponent,                   // ✅ standalone component
        HttpClientTestingModule,          // ✅ real HttpClient (mocked backend)
        RouterTestingModule.withRoutes([]) // ✅ router integration
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);

    // ✅ mock browser APIs
    jest.spyOn(window, 'alert').mockImplementation(() => {});
    jest.spyOn(console, 'error').mockImplementation(() => {});
    jest.spyOn(router, 'navigate').mockResolvedValue(true);

    fixture.detectChanges(); // renders template
  });

  afterEach(() => {
    httpMock.verify();
    jest.clearAllMocks();
  });

  // ---------------------------------------------------
  // SUCCESS FLOW – EMPLOYEE
  // ---------------------------------------------------
  it('should login employee and navigate to employee dashboard', () => {
    const el: HTMLElement = fixture.nativeElement;

    // 🔹 simulate user typing
    component.email = 'emp@test.com';
    component.password = '123456';

    fixture.detectChanges();

    // 🔹 click login button
    fixture.nativeElement.querySelector('button[type="submit"]')?.click();

    // 🔹 backend interception
    const req = httpMock.expectOne(
      'http://localhost:8080/api/auth/login'
    );

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      email: 'emp@test.com',
      password: '123456'
    });

    req.flush({
      role: 'EMPLOYEE',
      email: 'emp@test.com'
    });

    fixture.detectChanges();

    // 🔹 assertions
    expect(localStorage.getItem('loggedInUser')).toContain('EMPLOYEE');
    expect(router.navigate).toHaveBeenCalledWith(['/employee-dashboard']);
  });

  // ---------------------------------------------------
  // SUCCESS FLOW – ADMIN
  // ---------------------------------------------------
  it('should login admin and navigate to admin dashboard', () => {
    component.email = 'admin@test.com';
    component.password = 'admin123';

    fixture.nativeElement
      .querySelector('button[type="submit"]')
      ?.click();

    const req = httpMock.expectOne(
      'http://localhost:8080/api/auth/login'
    );

    req.flush({
      role: 'ADMIN',
      email: 'admin@test.com'
    });

    expect(router.navigate).toHaveBeenCalledWith(['/admin-dashboard']);
  });

  // ---------------------------------------------------
  // UNKNOWN ROLE
  // ---------------------------------------------------
  it('should show alert for unknown role', () => {
    component.login();

    const req = httpMock.expectOne(
      'http://localhost:8080/api/auth/login'
    );

    req.flush({
      role: 'MANAGER'
    });

    expect(window.alert).toHaveBeenCalledWith('Unknown role');
  });

  // ---------------------------------------------------
  // LOGIN FAILURE
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

    expect(window.alert).toHaveBeenCalledWith('Invalid email or password');
  });
});

import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController
} from '@angular/common/http/testing';
import { AuthService } from './auth.service';

describe('AuthService – Integration Tests', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const AUTH_API = 'http://localhost:8080/api/auth';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    // ✅ Prevents open HTTP calls & Jest leaks
    httpMock.verify();
  });

  // =====================================================
  // REGISTER
  // =====================================================
  it('should register a new user', () => {
    const payload = {
      firstName: 'John',
      lastName: 'Doe',
      email: 'john@test.com',
      password: 'password123'
    };

    const mockResponse = {
      message: 'User registered successfully'
    };

    service.register(payload).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(
      `${AUTH_API}/register`
    );

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);

    req.flush(mockResponse);
  });

  // =====================================================
  // LOGIN
  // =====================================================
  it('should login user with email and password', () => {
    const payload = {
      email: 'john@test.com',
      password: 'password123'
    };

    const mockResponse = {
      employeeId: 10,
      role: 'EMPLOYEE',
      email: 'john@test.com'
    };

    service.login(payload).subscribe(res => {
      expect(res).toEqual(mockResponse);
      expect(res.role).toBe('EMPLOYEE');
    });

    const req = httpMock.expectOne(
      `${AUTH_API}/login`
    );

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);

    req.flush(mockResponse);
  });

  // =====================================================
  // LOGIN FAILURE
  // =====================================================
  it('should propagate login error from backend', () => {
    const payload = {
      email: 'john@test.com',
      password: 'wrong-password'
    };

    service.login(payload).subscribe({
      next: () => fail('login should have failed'),
      error: err => {
        expect(err.status).toBe(401);
      }
    });

    const req = httpMock.expectOne(
      `${AUTH_API}/login`
    );

    expect(req.request.method).toBe('POST');

    req.flush(
      { message: 'Invalid credentials' },
      { status: 401, statusText: 'Unauthorized' }
    );
  });
});

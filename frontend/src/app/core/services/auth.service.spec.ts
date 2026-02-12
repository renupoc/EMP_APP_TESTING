import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const API = 'http://localhost:8080/api/auth';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    // ✅ prevents open handles / hanging Jest tests
    httpMock.verify();
  });

  // ---------------------------------------------------
  // REGISTER
  // ---------------------------------------------------
  it('should register a new user', () => {
    const payload = {
      firstName: 'John',
      lastName: 'Doe',
      email: 'john@test.com',
      password: '123456'
    };

    const mockResponse = { message: 'Registered successfully' };

    service.register(payload).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${API}/register`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);

    req.flush(mockResponse);
  });

  // ---------------------------------------------------
  // LOGIN
  // ---------------------------------------------------
  it('should login user with email and password', () => {
    const payload = {
      email: 'john@test.com',
      password: '123456'
    };

    const mockResponse = {
      role: 'EMPLOYEE',
      email: 'john@test.com'
    };

    service.login(payload).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${API}/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);

    req.flush(mockResponse);
  });
});

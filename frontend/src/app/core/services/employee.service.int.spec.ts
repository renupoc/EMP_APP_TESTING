import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController
} from '@angular/common/http/testing';
import { EmployeeService } from './employee.service';

describe('EmployeeService – Integration Tests', () => {
  let service: EmployeeService;
  let httpMock: HttpTestingController;

  const EMPLOYEE_API = 'http://localhost:8080/api/employees';
  const ATTENDANCE_API = 'http://localhost:8080/api/attendance';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [EmployeeService]
    });

    service = TestBed.inject(EmployeeService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    // ✅ Ensures no open HTTP calls (prevents Jest leaks)
    httpMock.verify();
  });

  // =====================================================
  // GET EMPLOYEE BY ID
  // =====================================================
  it('should fetch employee by id', () => {
    const mockEmployee = {
      id: 1,
      firstName: 'John',
      lastName: 'Doe',
      email: 'john@test.com'
    };

    service.getById(1).subscribe(res => {
      expect(res).toEqual(mockEmployee);
    });

    const req = httpMock.expectOne(`${EMPLOYEE_API}/1`);
    expect(req.request.method).toBe('GET');

    req.flush(mockEmployee);
  });

  // =====================================================
  // GET EMPLOYEE BY EMAIL
  // =====================================================
  it('should fetch employee by email', () => {
    const mockEmployee = {
      id: 2,
      email: 'user@test.com'
    };

    service.getByEmail('user@test.com').subscribe(res => {
      expect(res.email).toBe('user@test.com');
    });

    const req = httpMock.expectOne(
      `${EMPLOYEE_API}/by-email/user@test.com`
    );
    expect(req.request.method).toBe('GET');

    req.flush(mockEmployee);
  });

  // =====================================================
  // UPDATE DEPARTMENT
  // =====================================================
  it('should update employee department', () => {
    const response = { success: true };

    service.updateDepartment(5, 'IDIS').subscribe(res => {
      expect(res).toEqual(response);
    });

    const req = httpMock.expectOne(
      `${EMPLOYEE_API}/5/department`
    );

    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({
      department: 'IDIS'
    });

    req.flush(response);
  });

  // =====================================================
  // SAVE ATTENDANCE
  // =====================================================
  it('should save attendance for employee', () => {
    const payload = {
      month: 1,
      year: 2024,
      workedDays: 20
    };

    service.saveAttendance(10, payload).subscribe(res => {
      expect(res.success).toBe(true);
    });

    const req = httpMock.expectOne(
      `${ATTENDANCE_API}/10`
    );

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);

    req.flush({ success: true });
  });

  // =====================================================
  // GET ATTENDANCE
  // =====================================================
  it('should get attendance for employee', () => {
    const mockAttendance = [
      { id: 1, month: 1, year: 2024 }
    ];

    service.getAttendance(10).subscribe(res => {
      expect(res.length).toBe(1);
      expect(res[0].month).toBe(1);
    });

    const req = httpMock.expectOne(
      `${ATTENDANCE_API}/10`
    );

    expect(req.request.method).toBe('GET');
    req.flush(mockAttendance);
  });
});

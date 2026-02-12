import { TestBed } from '@angular/core/testing';
import { EmployeeService } from './employee.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

describe('EmployeeService', () => {
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
    // ✅ ensures no pending HTTP requests (prevents Jest leaks)
    httpMock.verify();
  });

  // ---------------------------------------------------
  // GET BY ID
  // ---------------------------------------------------
  it('should get employee by id', () => {
    const mockEmployee = {
      id: 1,
      name: 'John'
    };

    service.getById(1).subscribe(res => {
      expect(res).toEqual(mockEmployee);
    });

    const req = httpMock.expectOne(`${EMPLOYEE_API}/1`);
    expect(req.request.method).toBe('GET');

    req.flush(mockEmployee);
  });

  // ---------------------------------------------------
  // GET BY EMAIL
  // ---------------------------------------------------
  it('should get employee by email', () => {
    const mockEmployee = {
      email: 'john@test.com'
    };

    service.getByEmail('john@test.com').subscribe(res => {
      expect(res).toEqual(mockEmployee);
    });

    const req = httpMock.expectOne(
      `${EMPLOYEE_API}/by-email/john@test.com`
    );
    expect(req.request.method).toBe('GET');

    req.flush(mockEmployee);
  });

  // ---------------------------------------------------
  // UPDATE DEPARTMENT
  // ---------------------------------------------------
  it('should update employee department', () => {
    const mockResponse = { success: true };

    service.updateDepartment(1, 'IDIS').subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(
      `${EMPLOYEE_API}/1/department`
    );
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ department: 'IDIS' });

    req.flush(mockResponse);
  });

  // ---------------------------------------------------
  // SAVE ATTENDANCE
  // ---------------------------------------------------
  it('should save attendance for employee', () => {
    const payload = {
      date: '2024-01-01',
      status: 'PRESENT'
    };

    service.saveAttendance(1, payload).subscribe(res => {
      expect(res).toEqual({ success: true });
    });

    const req = httpMock.expectOne(
      `${ATTENDANCE_API}/1`
    );
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);

    req.flush({ success: true });
  });

  // ---------------------------------------------------
  // GET ATTENDANCE
  // ---------------------------------------------------
  it('should get attendance for employee', () => {
    const mockAttendance = [
      { date: '2024-01-01', status: 'PRESENT' }
    ];

    service.getAttendance(1).subscribe(res => {
      expect(res.length).toBe(1);
      expect(res).toEqual(mockAttendance);
    });

    const req = httpMock.expectOne(
      `${ATTENDANCE_API}/1`
    );
    expect(req.request.method).toBe('GET');

    req.flush(mockAttendance);
  });
});

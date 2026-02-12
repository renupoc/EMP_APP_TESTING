import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController
} from '@angular/common/http/testing';
import { AttendanceService, AttendancePayload, Attendance } from './attendance.service';

describe('AttendanceService – Integration Tests', () => {
  let service: AttendanceService;
  let httpMock: HttpTestingController;

  const BASE_URL = 'http://localhost:8080/api/attendance';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AttendanceService]
    });

    service = TestBed.inject(AttendanceService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    // ✅ Prevent open HTTP calls & Jest leaks
    httpMock.verify();
  });

  // =====================================================
  // EMPLOYEE – SUBMIT ATTENDANCE
  // =====================================================
  it('should submit attendance for employee', () => {
    const payload: AttendancePayload = {
      month: 1,
      year: 2024,
      totalDays: 31,
      workedDays: 20
    };

    service.submitAttendance(10, payload).subscribe(res => {
      expect(res.success).toBe(true);
    });

    const req = httpMock.expectOne(
      `${BASE_URL}/submit/10`
    );

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);

    req.flush({ success: true });
  });

  // =====================================================
  // EMPLOYEE – GET OWN ATTENDANCE
  // =====================================================
  it('should get attendance by employee', () => {
    const mockAttendance: Attendance[] = [
      {
        id: 1,
        month: 1,
        year: 2024,
        totalDays: 31,
        workingDays: 22,
        createdAt: '2024-01-31',
        employee: {
          id: 10,
          firstName: 'John',
          lastName: 'Doe',
          email: 'john@test.com',
          department: 'IDIS'
        }
      }
    ];

    service.getAttendanceByEmployee(10).subscribe(res => {
      expect(res.length).toBe(1);
      expect(res[0].employee.email).toBe('john@test.com');
    });

    const req = httpMock.expectOne(
      `${BASE_URL}/employee/10`
    );

    expect(req.request.method).toBe('GET');
    req.flush(mockAttendance);
  });

  // =====================================================
  // ADMIN – GET ALL ATTENDANCE
  // =====================================================
  it('should get all attendance for admin', () => {
    const mockAttendance: Attendance[] = [
      {
        id: 1,
        month: 1,
        year: 2024,
        totalDays: 31,
        workingDays: 22,
        createdAt: '2024-01-31',
        employee: {
          id: 10,
          firstName: 'John',
          lastName: 'Doe',
          email: 'john@test.com',
          department: 'IDIS'
        }
      }
    ];

    service.getAllAttendance().subscribe(res => {
      expect(res.length).toBeGreaterThan(0);
    });

    const req = httpMock.expectOne(
      `${BASE_URL}/admin/all`
    );

    expect(req.request.method).toBe('GET');
    req.flush(mockAttendance);
  });

  // =====================================================
  // EMPLOYEE – GET SELECTED DATES
  // =====================================================
  it('should get selected attendance dates', () => {
    const mockDates = ['2024-01-02', '2024-01-03'];

    service.getSelectedDates(10, 1, 2024).subscribe(res => {
      expect(res.length).toBe(2);
      expect(res).toContain('2024-01-02');
    });

    const req = httpMock.expectOne(req =>
      req.url === `${BASE_URL}/employee/10/days`
    );

    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('month')).toBe('1');
    expect(req.request.params.get('year')).toBe('2024');

    req.flush(mockDates);
  });
});

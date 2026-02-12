import { TestBed } from '@angular/core/testing';
import { AttendanceService, AttendancePayload, Attendance } from './attendance.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

describe('AttendanceService', () => {
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
    // ✅ Prevents open handles / async leaks
    httpMock.verify();
  });

  // ---------------------------------------------------
  // SUBMIT ATTENDANCE (EMPLOYEE)
  // ---------------------------------------------------
  it('should submit attendance for an employee', () => {
    const payload: AttendancePayload = {
      month: 1,
      year: 2024,
      totalDays: 31,
      workedDays: 22
    };

    service.submitAttendance(1, payload).subscribe(res => {
      expect(res).toEqual({ success: true });
    });

    const req = httpMock.expectOne(
      `${BASE_URL}/submit/1`
    );
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);

    req.flush({ success: true });
  });

  // ---------------------------------------------------
  // GET ATTENDANCE BY EMPLOYEE
  // ---------------------------------------------------
  it('should get attendance by employee id', () => {
    const mockAttendance: Attendance[] = [
      {
        id: 1,
        month: 1,
        year: 2024,
        totalDays: 31,
        workingDays: 22,
        createdAt: '2024-01-31',
        employee: {
          id: 1,
          firstName: 'John',
          lastName: 'Doe',
          email: 'john@test.com',
          department: 'IDIS'
        }
      }
    ];

    service.getAttendanceByEmployee(1).subscribe(res => {
      expect(res).toEqual(mockAttendance);
      expect(res.length).toBe(1);
    });

    const req = httpMock.expectOne(
      `${BASE_URL}/employee/1`
    );
    expect(req.request.method).toBe('GET');

    req.flush(mockAttendance);
  });

  // ---------------------------------------------------
  // GET ALL ATTENDANCE (ADMIN)
  // ---------------------------------------------------
  it('should get all attendance for admin', () => {
    const mockAttendance: Attendance[] = [];

    service.getAllAttendance().subscribe(res => {
      expect(res).toEqual(mockAttendance);
    });

    const req = httpMock.expectOne(
      `${BASE_URL}/admin/all`
    );
    expect(req.request.method).toBe('GET');

    req.flush(mockAttendance);
  });

  // ---------------------------------------------------
  // GET SELECTED DATES
  // ---------------------------------------------------
  it('should get selected attendance dates for an employee', () => {
    const mockDates = ['2024-01-01', '2024-01-02'];

    service.getSelectedDates(1, 1, 2024).subscribe(res => {
      expect(res).toEqual(mockDates);
    });

    const req = httpMock.expectOne(req =>
      req.url === `${BASE_URL}/employee/1/days`
    );

    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('month')).toBe('1');
    expect(req.request.params.get('year')).toBe('2024');

    req.flush(mockDates);
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ManagerDashboardComponent } from './manager-dashboard.component';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

describe('ManagerDashboardComponent – Integration Test', () => {
  let component: ManagerDashboardComponent;
  let fixture: ComponentFixture<ManagerDashboardComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ManagerDashboardComponent,   // ✅ standalone component
        HttpClientTestingModule
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ManagerDashboardComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);

    // ✅ mock browser APIs
    jest.spyOn(window, 'alert').mockImplementation(() => {});
    jest.spyOn(window, 'confirm').mockReturnValue(true);
    jest.spyOn(console, 'error').mockImplementation(() => {});

    fixture.detectChanges(); // triggers ngOnInit()
  });

  afterEach(() => {
    httpMock.verify();
    jest.clearAllMocks();
  });

  // --------------------------------------------------
  // INIT + LOAD EMPLOYEES
  // --------------------------------------------------
  it('should load employee attendance data on init', () => {
    const mockEmployees = [
      {
        attendanceId: 1,
        employeeId: 10,
        name: 'John',
        email: 'john@test.com',
        department: 'IDIS',
        month: 1,
        year: 2024,
        totalDays: 31,
        totalWorkingDays: 22,
        workedDays: 20
      }
    ];

    const req = httpMock.expectOne(
      'http://localhost:8080/api/attendance/admin/employees-attendance'
    );

    expect(req.request.method).toBe('GET');
    req.flush(mockEmployees);

    expect(component.employees.length).toBe(1);
    expect(component.years).toEqual([2024]);
  });

  // --------------------------------------------------
  // WEEKLY EXPAND
  // --------------------------------------------------
  it('should load weekly data when expanding employee', () => {
    const emp = {
      attendanceId: 1,
      employeeId: 10,
      name: 'John',
      email: 'john@test.com',
      department: 'IDIS',
      month: 1,
      year: 2024,
      totalDays: 31,
      totalWorkingDays: 22,
      workedDays: 20
    };

    component.employees = [emp];

    component.toggleWeeks(emp);

    const req = httpMock.expectOne(req =>
      req.url === 'http://localhost:8080/api/attendance/admin/weekly'
    );

    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('employeeId')).toBe('10');

    req.flush([
      {
        weekNumber: 1,
        start: '2024-01-01',
        end: '2024-01-07',
        totalWorkingDays: 5,
        workedDays: 4,
        availability: 80
      }
    ]);

    expect(component.weeklyData.length).toBe(1);
  });

  // --------------------------------------------------
  // SAVE WEEKLY UPDATE
  // --------------------------------------------------
  it('should update weekly attendance', () => {
    const emp = {
      attendanceId: 1,
      employeeId: 10,
      name: 'John',
      email: 'john@test.com',
      department: 'IDIS',
      month: 1,
      year: 2024,
      totalDays: 31,
      totalWorkingDays: 22,
      workedDays: 20
    };

    component.expandedEmployee = emp;
    component.editedWeekDays = 3;

    const week = {
      weekNumber: 1,
      start: '2024-01-01',
      end: '2024-01-07',
      totalWorkingDays: 5,
      workedDays: 4,
      availability: 80
    };

    component.saveWeek(week);

    const req = httpMock.expectOne(
      'http://localhost:8080/api/attendance/admin/weekly/update'
    );

    expect(req.request.method).toBe('PUT');
    expect(req.request.body.workedDays).toBe(3);

    req.flush({});

    // refresh calls
    httpMock.expectOne(
      'http://localhost:8080/api/attendance/admin/weekly'
    ).flush([]);

    httpMock.expectOne(
      'http://localhost:8080/api/attendance/admin/employees-attendance'
    ).flush([]);
  });

  // --------------------------------------------------
  // DELETE EMPLOYEE
  // --------------------------------------------------
  it('should delete employee record', () => {
    const emp = {
      attendanceId: 1,
      employeeId: 10,
      name: 'John',
      email: 'john@test.com',
      department: 'IDIS',
      month: 1,
      year: 2024,
      totalDays: 31,
      totalWorkingDays: 22,
      workedDays: 20
    };

    component.employees = [emp];

    component.deleteEmployee(emp);

    const req = httpMock.expectOne(
      'http://localhost:8080/api/attendance/admin/1'
    );

    expect(req.request.method).toBe('DELETE');
    req.flush({});

    expect(component.employees.length).toBe(0);
    expect(window.alert).toHaveBeenCalledWith('Deleted successfully');
  });

  // --------------------------------------------------
  // LOGOUT
  // --------------------------------------------------
  it('should logout and redirect', () => {
    delete (window as any).location;
    (window as any).location = { href: '' };

    component.logout();

    expect(window.location.href).toBe('/login');
  });
});

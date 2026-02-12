import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EmployeeDashboardComponent } from './employee-dashboard.component';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AttendanceService } from '../../core/services/attendance.service';
import { EmployeeService } from '../../core/services/employee.service';
import { By } from '@angular/platform-browser';

describe('EmployeeDashboardComponent – Integration Test', () => {
  let fixture: ComponentFixture<EmployeeDashboardComponent>;
  let component: EmployeeDashboardComponent;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    // ✅ fake logged-in user
    localStorage.setItem(
      'loggedInUser',
      JSON.stringify({ employeeId: 1, firstName: 'John' })
    );

    await TestBed.configureTestingModule({
      imports: [
        EmployeeDashboardComponent,   // ✅ standalone component
        HttpClientTestingModule
      ],
      providers: [
        AttendanceService,
        EmployeeService
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EmployeeDashboardComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);

    // ✅ mock browser APIs
    jest.spyOn(window, 'alert').mockImplementation(() => {});
    jest.spyOn(console, 'error').mockImplementation(() => {});

    fixture.detectChanges(); // triggers ngOnInit
  });

  afterEach(() => {
    httpMock.verify();
    jest.clearAllMocks();
    localStorage.clear();
  });

  // ---------------------------------------------------
  // INIT
  // ---------------------------------------------------
  it('should initialize user and generate years', () => {
    expect(component.user.employeeId).toBe(1);
    expect(component.years.length).toBeGreaterThan(0);
  });

  // ---------------------------------------------------
  // MONTH / YEAR CHANGE FLOW
  // ---------------------------------------------------
  it('should generate calendar, weeks and load saved dates', () => {
    component.selectedMonth = 1; // January
    component.selectedYear = 2024;

    component.onMonthOrYearChange();

    const req = httpMock.expectOne(req =>
      req.url ===
      'http://localhost:8080/api/attendance/employee/1/days'
    );

    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('month')).toBe('1');
    expect(req.request.params.get('year')).toBe('2024');

    req.flush(['2024-01-02', '2024-01-03']);

    expect(component.calendarDays.length).toBeGreaterThan(0);
    expect(component.weeks.length).toBeGreaterThan(0);
    expect(component.workedDays).toBeGreaterThan(0);
  });

  // ---------------------------------------------------
  // TOGGLE DAY
  // ---------------------------------------------------
  it('should toggle a working day and update counts', () => {
    component.selectedMonth = 1;
    component.selectedYear = 2024;
    component.onMonthOrYearChange();

    httpMock.expectOne(() => true).flush([]);

    const workingDay = component.calendarDays.find(
      d => !d.isWeekend && d.day === 2
    )!;

    component.toggleDay(workingDay);

    expect(workingDay.isSelected).toBe(true);
    expect(component.workedDays).toBe(1);
  });

  // ---------------------------------------------------
  // SUBMIT ATTENDANCE
  // ---------------------------------------------------
  it('should submit attendance successfully', () => {
    component.selectedMonth = 1;
    component.selectedYear = 2024;
    component.onMonthOrYearChange();

    httpMock.expectOne(() => true).flush([]);

    const workingDay = component.calendarDays.find(
      d => !d.isWeekend && d.day === 2
    )!;
    component.toggleDay(workingDay);

    component.submit();

    const req = httpMock.expectOne(
      'http://localhost:8080/api/attendance/submit/1'
    );

    expect(req.request.method).toBe('POST');
    expect(req.request.body.workedDays).toBe(1);
    expect(req.request.body.selectedDates.length).toBe(1);

    req.flush({});

    expect(window.alert).toHaveBeenCalledWith(
      'Attendance submitted successfully'
    );
  });

  // ---------------------------------------------------
  // SUBMIT FAILURE
  // ---------------------------------------------------
  it('should show alert if submit fails', () => {
    component.selectedMonth = 1;
    component.selectedYear = 2024;
    component.onMonthOrYearChange();

    httpMock.expectOne(() => true).flush([]);

    const workingDay = component.calendarDays.find(
      d => !d.isWeekend && d.day === 2
    )!;
    component.toggleDay(workingDay);

    component.submit();

    const req = httpMock.expectOne(
      'http://localhost:8080/api/attendance/submit/1'
    );

    req.flush({}, { status: 500, statusText: 'Server Error' });

    expect(window.alert).toHaveBeenCalledWith('Submit failed');
  });

  // ---------------------------------------------------
  // LOGOUT
  // ---------------------------------------------------
  it('should logout user and redirect', () => {
    delete (window as any).location;
    (window as any).location = { href: '' };

    component.logout();

    expect(localStorage.getItem('loggedInUser')).toBeNull();
    expect(window.location.href).toBe('/login');
  });
});

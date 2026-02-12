import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ManagerDashboardComponent } from './manager-dashboard.component';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';

describe('ManagerDashboardComponent (Unit)', () => {
  let component: ManagerDashboardComponent;
  let fixture: ComponentFixture<ManagerDashboardComponent>;
  let httpMock: HttpTestingController;

  const BASE_URL =
    'http://localhost:8080/api/attendance/admin/employees-attendance';

  const WEEK_URL =
    'http://localhost:8080/api/attendance/admin/weekly';

  const WEEK_UPDATE_URL =
    'http://localhost:8080/api/attendance/admin/weekly/update';

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ManagerDashboardComponent,
        HttpClientTestingModule
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(ManagerDashboardComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);

    jest.spyOn(window, 'alert').mockImplementation(() => {});
    jest.spyOn(window, 'confirm').mockReturnValue(true);
    jest.spyOn(console, 'error').mockImplementation(() => {});

    Object.defineProperty(window, 'location', {
      writable: true,
      value: { href: '' }
    });

    fixture.detectChanges();

    // 🔥 Flush ngOnInit call
    httpMock.expectOne(BASE_URL).flush([]);
  });

  afterEach(() => {
    httpMock.verify();
    jest.clearAllMocks();
  });

  // --------------------------------------------------
  // BASIC
  // --------------------------------------------------

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  // --------------------------------------------------
  // LOAD EMPLOYEES
  // --------------------------------------------------

  it('should load employees', () => {
    component.loadEmployees();

    const req = httpMock.expectOne(BASE_URL);
    expect(req.request.method).toBe('GET');

    req.flush([
      {
        attendanceId: 1,
        employeeId: 10,
        name: 'John',
        email: 'john@test.com',
        department: 'IDIS',
        month: 1,
        year: 2024,
        totalDays: 30,
        totalWorkingDays: 22,
        workedDays: 20
      }
    ]);

    expect(component.employees.length).toBe(1);
    expect(component.years).toEqual([2024]);
  });

  it('should show alert on load failure', () => {
    component.loadEmployees();

    const req = httpMock.expectOne(BASE_URL);
    req.flush('error', { status: 500, statusText: 'Server Error' });

    expect(window.alert).toHaveBeenCalledWith('Failed to load attendance data');
  });

  // --------------------------------------------------
  // FILTER
  // --------------------------------------------------

  it('should filter employees by department', () => {
    component.employees = [
      { department: 'IDIS' } as any,
      { department: 'EMRI' } as any
    ];

    component.selectedDepartment = 'IDIS';

    expect(component.filteredEmployees().length).toBe(1);
  });

  // --------------------------------------------------
  // HELPERS
  // --------------------------------------------------

  it('should calculate availability percent', () => {
    const emp = { workedDays: 10, totalWorkingDays: 20 } as any;
    expect(component.availabilityPercent(emp)).toBe(50);
  });

  // --------------------------------------------------
  // WEEKLY TOGGLE
  // --------------------------------------------------

  it('should load weekly data', () => {
    const emp = {
      employeeId: 10,
      month: 1,
      year: 2024
    } as any;

    component.toggleWeeks(emp);

    const req = httpMock.expectOne(WEEK_URL);
    expect(req.request.method).toBe('GET');

    req.flush([]);

    expect(component.expandedEmployee).toBe(emp);
  });

  it('should collapse weekly if same employee clicked', () => {
    const emp = {} as any;
    component.expandedEmployee = emp;
    component.weeklyData = [{} as any];

    component.toggleWeeks(emp);

    expect(component.expandedEmployee).toBeNull();
    expect(component.weeklyData.length).toBe(0);
  });

  // --------------------------------------------------
  // MONTHLY EDIT
  // --------------------------------------------------

  it('should save employee update', () => {
    component.editingEmployee = {
      attendanceId: 1,
      totalWorkingDays: 22,
      workedDays: 10,
      department: 'IDIS'
    } as any;

    component.editModel = {
      department: 'EMRI',
      workingDays: 20
    };

    component.saveEmployee();

    const req = httpMock.expectOne(
      'http://localhost:8080/api/attendance/admin/update/1'
    );

    expect(req.request.method).toBe('PUT');
    req.flush({});

    expect(component.editingEmployee).toBeNull();
    expect(window.alert).toHaveBeenCalledWith('Saved successfully');
  });

  // --------------------------------------------------
  // WEEKLY UPDATE
  // --------------------------------------------------

  it('should update weekly attendance', () => {
    component.expandedEmployee = {
      employeeId: 10,
      month: 1,
      year: 2024
    } as any;

    component.editedWeekDays = 3;

    component.saveWeek({
      weekNumber: 1,
      totalWorkingDays: 5
    } as any);

    httpMock.expectOne(WEEK_UPDATE_URL).flush({});
    httpMock.expectOne(WEEK_URL).flush([]);
    httpMock.expectOne(BASE_URL).flush([]);

    expect(window.alert).toHaveBeenCalledWith('Weekly attendance updated');
  });

  // --------------------------------------------------
  // DELETE
  // --------------------------------------------------

  it('should delete employee', () => {
    component.employees = [
      { attendanceId: 1 } as any,
      { attendanceId: 2 } as any
    ];

    component.deleteEmployee({ attendanceId: 1 } as any);

    const req = httpMock.expectOne(
      'http://localhost:8080/api/attendance/admin/1'
    );

    expect(req.request.method).toBe('DELETE');
    req.flush({});

    expect(component.employees.length).toBe(1);
    expect(window.alert).toHaveBeenCalledWith('Deleted successfully');
  });

  // --------------------------------------------------
  // LOGOUT
  // --------------------------------------------------

  it('should logout and redirect', () => {
    const spy = jest.spyOn(sessionStorage, 'clear');

    component.logout();

    expect(spy).toHaveBeenCalled();
    expect(window.location.href).toBe('/login');
  });
});

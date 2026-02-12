import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ManagerDashboardComponent } from './manager-dashboard.component';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';

describe('ManagerDashboardComponent', () => {
  let component: ManagerDashboardComponent;
  let fixture: ComponentFixture<ManagerDashboardComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ManagerDashboardComponent, // ✅ standalone component
        HttpClientTestingModule
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(ManagerDashboardComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);

    // ✅ Mock browser APIs
    jest.spyOn(window, 'alert').mockImplementation(() => {});
    jest.spyOn(window, 'confirm').mockReturnValue(true);

    fixture.detectChanges(); // triggers ngOnInit → loadEmployees()
  });

  afterEach(() => {
    httpMock.verify(); // ✅ prevents open handles
    jest.clearAllMocks();
  });

  // ---------------------------------------------------
  // BASIC
  // ---------------------------------------------------
  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  // ---------------------------------------------------
  // LOAD EMPLOYEES
  // ---------------------------------------------------
  it('should load employees on init', () => {
    const mockEmployees = [
      {
        attendanceId: 1,
        employeeId: 101,
        name: 'John',
        email: 'john@test.com',
        department: 'IDIS',
        month: 1,
        year: 2024,
        totalDays: 30,
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

  it('should alert on loadEmployees failure', () => {
    const req = httpMock.expectOne(
      'http://localhost:8080/api/attendance/admin/employees-attendance'
    );

    req.flush('error', { status: 500, statusText: 'Server Error' });

    expect(window.alert).toHaveBeenCalledWith('Failed to load attendance data');
  });

  // ---------------------------------------------------
  // FILTERED EMPLOYEES
  // ---------------------------------------------------
  it('should filter employees by department', () => {
    component.employees = [
      { department: 'IDIS' } as any,
      { department: 'EMRI' } as any
    ];

    component.selectedDepartment = 'IDIS';

    expect(component.filteredEmployees().length).toBe(1);
  });

  // ---------------------------------------------------
  // HELPERS
  // ---------------------------------------------------
  it('should calculate availability percent', () => {
    const emp = { workedDays: 10, totalWorkingDays: 20 } as any;
    expect(component.availabilityPercent(emp)).toBe(50);
  });

  // ---------------------------------------------------
  // WEEKLY TOGGLE
  // ---------------------------------------------------
  it('should load weekly data when toggling weeks', () => {
    const emp = {
      employeeId: 1,
      month: 1,
      year: 2024
    } as any;

    component.toggleWeeks(emp);

    const req = httpMock.expectOne(req =>
      req.url === 'http://localhost:8080/api/attendance/admin/weekly'
    );

    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('employeeId')).toBe('1');

    req.flush([]);

    expect(component.expandedEmployee).toBe(emp);
  });

  it('should collapse weekly view if same employee clicked', () => {
    const emp = {} as any;

    component.expandedEmployee = emp;
    component.weeklyData = [{} as any];

    component.toggleWeeks(emp);

    expect(component.expandedEmployee).toBeNull();
    expect(component.weeklyData.length).toBe(0);
  });

  // ---------------------------------------------------
  // MONTHLY EDIT
  // ---------------------------------------------------
  it('should edit employee', () => {
    const emp = {
      department: 'IDIS',
      workedDays: 10
    } as any;

    component.editEmployee(emp);

    expect(component.editingEmployee).toBe(emp);
    expect(component.editModel.workingDays).toBe(10);
  });

  it('should save employee update', () => {
    component.editingEmployee = {
      attendanceId: 1,
      totalWorkingDays: 22
    } as any;

    component.editModel = {
      department: 'IDIS',
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

  // ---------------------------------------------------
  // WEEKLY EDIT
  // ---------------------------------------------------
  it('should save weekly update', () => {
    component.expandedEmployee = {
      employeeId: 1,
      month: 1,
      year: 2024
    } as any;

    component.editedWeekDays = 4;

    const week = {
      weekNumber: 1,
      totalWorkingDays: 5
    } as any;

    component.saveWeek(week);

    const req = httpMock.expectOne(
      'http://localhost:8080/api/attendance/admin/weekly/update'
    );

    expect(req.request.method).toBe('PUT');
    req.flush({});

    expect(window.alert).toHaveBeenCalledWith('Weekly attendance updated');
  });

  // ---------------------------------------------------
  // DELETE
  // ---------------------------------------------------
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

  // ---------------------------------------------------
  // LOGOUT
  // ---------------------------------------------------
  it('should logout and redirect', () => {
    const spy = jest.spyOn(sessionStorage, 'clear');

    delete (window as any).location;
    (window as any).location = { href: '' };

    component.logout();

    expect(spy).toHaveBeenCalled();
    expect(window.location.href).toBe('/login');
  });
});

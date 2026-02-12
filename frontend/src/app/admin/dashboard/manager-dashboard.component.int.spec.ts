import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ManagerDashboardComponent } from './manager-dashboard.component';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

describe('ManagerDashboardComponent (Integration)', () => {
  let component: ManagerDashboardComponent;
  let fixture: ComponentFixture<ManagerDashboardComponent>;
  let httpMock: HttpTestingController;

  const BASE_URL =
    'http://localhost:8080/api/attendance/admin/employees-attendance';

  const WEEK_URL =
    'http://localhost:8080/api/attendance/admin/weekly';

  const WEEK_UPDATE_URL =
    'http://localhost:8080/api/attendance/admin/weekly/update';

  const DELETE_URL =
    'http://localhost:8080/api/attendance/admin/1';

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ManagerDashboardComponent,
        HttpClientTestingModule
      ]
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

    // ✅ IMPORTANT: Always flush ngOnInit request
    httpMock.expectOne(BASE_URL).flush([]);
  });

  afterEach(() => {
    httpMock.verify();
    jest.clearAllMocks();
  });

  // --------------------------------------------------
  // INIT
  // --------------------------------------------------

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  // --------------------------------------------------
  // WEEKLY FLOW
  // --------------------------------------------------

  it('should expand weekly and load data', () => {
    const emp = {
      attendanceId: 1,
      employeeId: 10,
      month: 1,
      year: 2024
    } as any;

    component.toggleWeeks(emp);

    const weekReq = httpMock.expectOne(WEEK_URL);
    expect(weekReq.request.method).toBe('GET');

    weekReq.flush([]);

    expect(component.expandedEmployee).toBe(emp);
  });

  // --------------------------------------------------
  // WEEK UPDATE FLOW
  // --------------------------------------------------

  it('should update weekly and refresh data', () => {
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

    const updateReq = httpMock.expectOne(WEEK_UPDATE_URL);
    expect(updateReq.request.method).toBe('PUT');
    updateReq.flush({});

    httpMock.expectOne(WEEK_URL).flush([]);
    httpMock.expectOne(BASE_URL).flush([]);

    expect(window.alert).toHaveBeenCalledWith('Weekly attendance updated');
  });

  // --------------------------------------------------
  // DELETE FLOW
  // --------------------------------------------------

  it('should delete employee', () => {
    component.employees = [
      { attendanceId: 1 } as any,
      { attendanceId: 2 } as any
    ];

    component.deleteEmployee({ attendanceId: 1 } as any);

    const deleteReq = httpMock.expectOne(DELETE_URL);
    expect(deleteReq.request.method).toBe('DELETE');
    deleteReq.flush({});

    expect(component.employees.length).toBe(1);
  });

  // --------------------------------------------------
  // LOGOUT
  // --------------------------------------------------

  it('should logout properly', () => {
    const spy = jest.spyOn(sessionStorage, 'clear');

    component.logout();

    expect(spy).toHaveBeenCalled();
    expect(window.location.href).toBe('/login');
  });
});

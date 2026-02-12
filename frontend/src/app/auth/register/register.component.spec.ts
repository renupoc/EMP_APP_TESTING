import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RegisterComponent } from './register.component';
import { AuthService } from '../../core/services/auth.service';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';

describe('RegisterComponent (Jest)', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let authService: jest.Mocked<AuthService>;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        RegisterComponent,                 // ✅ standalone component
        RouterTestingModule.withRoutes([]) // ✅ fixes ActivatedRoute
      ],
      providers: [
        {
          provide: AuthService,
          useValue: {
            register: jest.fn()
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;

    authService = TestBed.inject(AuthService) as jest.Mocked<AuthService>;
    router = TestBed.inject(Router);

    // ✅ Mock browser APIs
    jest.spyOn(window, 'alert').mockImplementation(() => {});
    jest.spyOn(router, 'navigate').mockResolvedValue(true);

    fixture.detectChanges();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  // ---------------------------------------------------
  // BASIC
  // ---------------------------------------------------
  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  // ---------------------------------------------------
  // VALIDATION
  // ---------------------------------------------------
  it('should show alert if required fields are missing', () => {
    component.register();

    expect(window.alert).toHaveBeenCalledWith('All fields are required');
    expect(authService.register).not.toHaveBeenCalled();
  });

  it('should show alert if passwords do not match', () => {
    component.firstName = 'John';
    component.lastName = 'Doe';
    component.email = 'john@test.com';
    component.password = '123456';
    component.confirmPassword = '654321';

    component.register();

    expect(window.alert).toHaveBeenCalledWith('Passwords do not match');
    expect(authService.register).not.toHaveBeenCalled();
  });

  // ---------------------------------------------------
  // SUCCESS
  // ---------------------------------------------------
  it('should register user successfully and navigate to login', () => {
    authService.register.mockReturnValue(of({}));

    component.firstName = 'John';
    component.lastName = 'Doe';
    component.email = 'john@test.com';
    component.department = 'IDIS';
    component.password = '123456';
    component.confirmPassword = '123456';

    component.register();

    expect(authService.register).toHaveBeenCalledWith({
      firstName: 'John',
      lastName: 'Doe',
      email: 'john@test.com',
      department: 'IDIS',
      password: '123456'
    });

    expect(window.alert).toHaveBeenCalledWith('Registration successful');
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  // ---------------------------------------------------
  // FAILURE
  // ---------------------------------------------------
  it('should show error alert on registration failure', () => {
    authService.register.mockReturnValue(
      throwError(() => new Error('Registration failed'))
    );

    component.firstName = 'John';
    component.lastName = 'Doe';
    component.email = 'john@test.com';
    component.password = '123456';
    component.confirmPassword = '123456';

    component.register();

    expect(window.alert).toHaveBeenCalledWith('Registration failed');
  });
});

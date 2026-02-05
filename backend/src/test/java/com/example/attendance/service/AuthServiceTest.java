package com.example.attendance.service;

import com.example.attendance.dto.LoginRequest;
import com.example.attendance.dto.RegisterRequest;
import com.example.attendance.entity.Employee;
import com.example.attendance.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AuthService authService;

    // ================= REGISTER =================

    @Test
    void register_shouldSaveEmployee_whenEmailNotExists() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Renu");
        request.setLastName("K");
        request.setEmail("renu@test.com");
        request.setPassword("1234");
        request.setDepartment("IDIS");

        when(employeeRepository.existsByEmail("renu@test.com"))
                .thenReturn(false);
        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Employee saved = authService.register(request);

        assertNotNull(saved);
        assertEquals("Renu", saved.getFirstName());
        assertEquals("K", saved.getLastName());
        assertEquals("Renu K", saved.getFullName());
        assertEquals("renu@test.com", saved.getEmail());
        assertEquals("IDIS", saved.getDepartment());
        assertEquals("EMPLOYEE", saved.getRole());

        verify(employeeRepository).existsByEmail("renu@test.com");
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("renu@test.com");

        when(employeeRepository.existsByEmail("renu@test.com"))
                .thenReturn(true);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.register(request)
        );

        assertEquals("Email already registered", ex.getMessage());
        verify(employeeRepository, never()).save(any());
    }

    // ================= LOGIN =================

    @Test
    void login_shouldReturnResponseMap_whenCredentialsValid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("renu@test.com");
        request.setPassword("1234");

        Employee employee = new Employee();
        employee.setId(1L);
        employee.setFirstName("Renu");
        employee.setLastName("K");
        employee.setFullName("Renu K"); // IMPORTANT: login() does NOT compute this
        employee.setEmail("renu@test.com");
        employee.setPassword("1234");
        employee.setDepartment("VEDC");
        employee.setRole("EMPLOYEE");

        when(employeeRepository.findByEmail("renu@test.com"))
                .thenReturn(Optional.of(employee));

        Map<String, Object> response = authService.login(request);

        assertEquals(1L, response.get("employeeId"));
        assertEquals("Renu K", response.get("fullName"));
        assertEquals("renu@test.com", response.get("email"));
        assertEquals("VEDC", response.get("department"));
        assertEquals("EMPLOYEE", response.get("role"));
    }

    @Test
    void login_shouldThrowException_whenPasswordIsWrong() {
        LoginRequest request = new LoginRequest();
        request.setEmail("renu@test.com");
        request.setPassword("wrong");

        Employee employee = new Employee();
        employee.setPassword("1234");

        when(employeeRepository.findByEmail("renu@test.com"))
                .thenReturn(Optional.of(employee));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.login(request)
        );

        assertEquals("Invalid credentials", ex.getMessage());
    }

    @Test
    void login_shouldThrowException_whenEmailNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("missing@test.com");
        request.setPassword("1234");

        when(employeeRepository.findByEmail("missing@test.com"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.login(request)
        );

        assertEquals("Invalid credentials", ex.getMessage());
    }
}

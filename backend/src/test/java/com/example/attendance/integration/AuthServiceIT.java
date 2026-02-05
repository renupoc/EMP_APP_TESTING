package com.example.attendance.integration;

import com.example.attendance.dto.LoginRequest;
import com.example.attendance.dto.RegisterRequest;
import com.example.attendance.entity.Employee;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.service.AuthService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AuthServiceIT {

    @Autowired
    private AuthService authService;

    @Autowired
    private EmployeeRepository employeeRepository;

    // =====================================================
    // REGISTER – success
    // =====================================================
    @Test
    void register_shouldSaveEmployeeInDatabase() {

        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Renu");
        request.setLastName("K");
        request.setEmail("renu@test.com");
        request.setPassword("1234");
        request.setDepartment("IDIS");

        Employee saved = authService.register(request);

        assertNotNull(saved.getId());
        assertEquals("Renu K", saved.getFullName());
        assertEquals("EMPLOYEE", saved.getRole());

        // Verify persisted
        Employee fromDb =
                employeeRepository.findById(saved.getId()).orElseThrow();

        assertEquals("renu@test.com", fromDb.getEmail());
    }

    // =====================================================
    // REGISTER – email already exists
    // =====================================================
    @Test
    void register_whenEmailExists_shouldThrowException() {

        Employee emp = new Employee();
        emp.setFirstName("Renu");
        emp.setLastName("K");
        emp.setFullName("Renu K");
        emp.setEmail("renu@test.com");
        emp.setPassword("1234");
        emp.setDepartment("IDIS");
        emp.setRole("EMPLOYEE");

        employeeRepository.save(emp);

        RegisterRequest request = new RegisterRequest();
        request.setEmail("renu@test.com");

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.register(request)
        );

        assertEquals("Email already registered", ex.getMessage());
    }

    // =====================================================
    // LOGIN – success
    // =====================================================
    @Test
    void login_shouldReturnEmployeeDetails_whenCredentialsValid() {

        Employee emp = new Employee();
        emp.setFirstName("Renu");
        emp.setLastName("K");
        emp.setFullName("Renu K");
        emp.setEmail("renu@test.com");
        emp.setPassword("1234");
        emp.setDepartment("VEDC");
        emp.setRole("EMPLOYEE");

        employeeRepository.save(emp);

        LoginRequest request = new LoginRequest();
        request.setEmail("renu@test.com");
        request.setPassword("1234");

        Map<String, Object> response = authService.login(request);

        assertEquals("Renu K", response.get("fullName"));
        assertEquals("VEDC", response.get("department"));
        assertEquals("EMPLOYEE", response.get("role"));
    }

    // =====================================================
    // LOGIN – wrong password
    // =====================================================
    @Test
    void login_whenPasswordInvalid_shouldThrowException() {

        Employee emp = new Employee();
        emp.setEmail("renu@test.com");
        emp.setPassword("1234");

        employeeRepository.save(emp);

        LoginRequest request = new LoginRequest();
        request.setEmail("renu@test.com");
        request.setPassword("wrong");

        assertThrows(RuntimeException.class, () ->
                authService.login(request)
        );
    }

    // =====================================================
    // LOGIN – email not found
    // =====================================================
    @Test
    void login_whenEmailNotFound_shouldThrowException() {

        LoginRequest request = new LoginRequest();
        request.setEmail("missing@test.com");
        request.setPassword("1234");

        assertThrows(RuntimeException.class, () ->
                authService.login(request)
        );
    }
}

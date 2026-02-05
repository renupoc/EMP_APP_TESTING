package com.example.attendance.integration;

import com.example.attendance.entity.Employee;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.service.EmployeeService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class EmployeeServiceIT {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    // =====================================================
    // getEmployee – integration
    // =====================================================
    @Test
    void getEmployee_shouldReturnEmployeeFromDatabase() {

        // GIVEN – real DB record
        Employee employee = new Employee();
        employee.setFirstName("Renu");
        employee.setLastName("K");
        employee.setEmail("renu@test.com");
        employee.setDepartment("IDIS");

        employee = employeeRepository.save(employee);

        // WHEN
        Employee result = employeeService.getEmployee(employee.getId());

        // THEN
        assertNotNull(result);
        assertEquals("Renu", result.getFirstName());
        assertEquals("IDIS", result.getDepartment());
    }

    // =====================================================
    // getEmployee – negative path
    // =====================================================
    @Test
    void getEmployee_whenEmployeeNotFound_shouldThrowException() {

        assertThrows(RuntimeException.class, () ->
                employeeService.getEmployee(999L)
        );
    }

    // =====================================================
    // updateEmployee – integration
    // =====================================================
    @Test
    void updateEmployee_shouldUpdateEmployeeInDatabase() {

        // GIVEN
        Employee employee = new Employee();
        employee.setFirstName("Old");
        employee.setLastName("Name");
        employee.setEmail("old@test.com");
        employee.setDepartment("EMSS");

        employee = employeeRepository.save(employee);

        Employee updated = new Employee();
        updated.setFirstName("New");
        updated.setLastName("Name");
        updated.setDepartment("VEDC");

        // WHEN
        Employee result =
                employeeService.updateEmployee(employee.getId(), updated);

        // THEN
        assertNotNull(result);
        assertEquals("New", result.getFirstName());
        assertEquals("VEDC", result.getDepartment());

        // Verify persisted change
        Employee fromDb =
                employeeRepository.findById(employee.getId()).orElseThrow();

        assertEquals("New", fromDb.getFirstName());
        assertEquals("VEDC", fromDb.getDepartment());
    }

    // =====================================================
    // updateEmployee – negative path
    // =====================================================
    @Test
    void updateEmployee_whenEmployeeNotFound_shouldThrowException() {

        Employee updated = new Employee();
        updated.setFirstName("X");
        updated.setLastName("Y");
        updated.setDepartment("GUSS");

        assertThrows(RuntimeException.class, () ->
                employeeService.updateEmployee(999L, updated)
        );
    }
}

package com.example.attendance.service;

import com.example.attendance.entity.Employee;
import com.example.attendance.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    // ---------- getEmployee() ----------

    @Test
    void getEmployee_shouldReturnEmployee_whenEmployeeExists() {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setFirstName("Renu");

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        Employee result = employeeService.getEmployee(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Renu", result.getFirstName());
    }

    @Test
    void getEmployee_shouldThrowException_whenEmployeeNotFound() {
        when(employeeRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> employeeService.getEmployee(1L)
        );

        verify(employeeRepository).findById(1L);
    }

    // ---------- updateEmployee() ----------

    @Test
    void updateEmployee_shouldUpdateAndSaveEmployee_whenEmployeeExists() {
        Employee existing = new Employee();
        existing.setId(1L);
        existing.setFirstName("Old");
        existing.setLastName("Name");
        existing.setDepartment("HR");

        Employee updated = new Employee();
        updated.setFirstName("New");
        updated.setLastName("Name");
        updated.setDepartment("IT");

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(existing));
        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Employee result = employeeService.updateEmployee(1L, updated);

        assertEquals("New", result.getFirstName());
        assertEquals("Name", result.getLastName());
        assertEquals("IT", result.getDepartment());

        verify(employeeRepository).findById(1L);
        verify(employeeRepository).save(existing);
    }

    @Test
    void updateEmployee_shouldThrowException_whenEmployeeNotFound() {
        Employee updated = new Employee();

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> employeeService.updateEmployee(1L, updated)
        );

        verify(employeeRepository).findById(1L);
        verify(employeeRepository, never()).save(any());
    }
}

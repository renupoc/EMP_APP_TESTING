package com.example.attendance.service;

import com.example.attendance.dto.LoginRequest;
import com.example.attendance.dto.RegisterRequest;
import com.example.attendance.entity.Employee;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.exception.InvalidCredentialsException;
import com.example.attendance.exception.EmailAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final EmployeeRepository employeeRepository;

    public AuthService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    // REGISTER
    public Employee register(RegisterRequest request) {

        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        Employee emp = new Employee();
        emp.setFirstName(request.getFirstName());
        emp.setLastName(request.getLastName());
        emp.setFullName(request.getFirstName() + " " + request.getLastName());
        emp.setEmail(request.getEmail());
        emp.setPassword(request.getPassword());
        emp.setDepartment(request.getDepartment());
        emp.setRole("EMPLOYEE");

        return employeeRepository.save(emp);
    }

    // LOGIN
    public Map<String, Object> login(LoginRequest request) {

        log.debug("Login attempt for email: {}", request.getEmail());

        Employee employee = employeeRepository
                .findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!employee.getPassword().equals(request.getPassword())) {
            throw new InvalidCredentialsException();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("employeeId", employee.getId());
        response.put("fullName", employee.getFullName());
        response.put("email", employee.getEmail());
        response.put("department", employee.getDepartment());
        response.put("role", employee.getRole());

        return response;
    }
}
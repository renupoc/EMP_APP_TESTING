package com.example.attendance.integration;

import com.example.attendance.dto.LoginRequest;
import com.example.attendance.dto.RegisterRequest;
import com.example.attendance.entity.Employee;
import com.example.attendance.repository.EmployeeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // ===============================
    // REGISTER – integration test
    // ===============================
    @Test
    void register_shouldCreateEmployee_inRealDB() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Renu");
        request.setLastName("K");
        request.setEmail("renu@test.com");
        request.setPassword("1234");
        request.setDepartment("IDIS");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("renu@test.com"))
                .andExpect(jsonPath("$.fullName").value("Renu K"))
                .andExpect(jsonPath("$.department").value("IDIS"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"));
    }

    // ===============================
    // LOGIN – integration test
    // ===============================
    @Test
    void login_shouldReturnEmployeeDetails_whenCredentialsValid() throws Exception {

        // GIVEN – employee exists in DB
        Employee employee = new Employee();
        employee.setFirstName("Renu");
        employee.setLastName("K");
        employee.setFullName("Renu K");
        employee.setEmail("renu@test.com");
        employee.setPassword("1234");
        employee.setDepartment("VEDC");
        employee.setRole("EMPLOYEE");

        employeeRepository.save(employee);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("renu@test.com");
        loginRequest.setPassword("1234");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("renu@test.com"))
                .andExpect(jsonPath("$.fullName").value("Renu K"))
                .andExpect(jsonPath("$.department").value("VEDC"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"));
    }

    // ===============================
    // LOGIN – negative integration test
    // ===============================
    @Test
    void login_shouldFail_whenPasswordIsWrong() throws Exception {

        Employee employee = new Employee();
        employee.setEmail("renu@test.com");
        employee.setPassword("1234");
        employee.setFullName("Renu K");

        employeeRepository.save(employee);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("renu@test.com");
        loginRequest.setPassword("wrong");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError());
    }
}

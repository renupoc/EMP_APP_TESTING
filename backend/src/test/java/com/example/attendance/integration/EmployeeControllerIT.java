package com.example.attendance.integration;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmployeeControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // ===============================
    // GET employee by ID – success
    // ===============================
    @Test
    void getEmployeeProfile_shouldReturnEmployee_whenIdExists() throws Exception {

        // GIVEN – real DB data
        Employee employee = new Employee();
        employee.setFirstName("Renu");
        employee.setLastName("K");
        employee.setEmail("renu@test.com");
        employee.setDepartment("IDIS");

        employee = employeeRepository.save(employee);

        // WHEN + THEN
        mockMvc.perform(get("/api/employees/by-id/{id}", employee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Renu"))
                .andExpect(jsonPath("$.lastName").value("K"))
                .andExpect(jsonPath("$.email").value("renu@test.com"))
                .andExpect(jsonPath("$.department").value("IDIS"));
    }

    // ===============================
    // GET employee by ID – not found
    // ===============================
    @Test
    void getEmployeeProfile_shouldFail_whenEmployeeNotFound() throws Exception {

        mockMvc.perform(get("/api/employees/by-id/{id}", 999L))
                .andExpect(status().isInternalServerError());
    }

    // ===============================
    // UPDATE department – success
    // ===============================
    @Test
    void updateDepartment_shouldUpdateDepartment_inRealDB() throws Exception {

        // GIVEN
        Employee employee = new Employee();
        employee.setFirstName("Renu");
        employee.setLastName("K");
        employee.setEmail("renu@test.com");
        employee.setDepartment("IDIS");

        employee = employeeRepository.save(employee);

        Employee request = new Employee();
        request.setDepartment("VEDC");

        // WHEN + THEN
        mockMvc.perform(put("/api/employees/{id}/department", employee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.department").value("VEDC"));
    }

    // ===============================
    // UPDATE department – employee not found
    // ===============================
    @Test
    void updateDepartment_shouldFail_whenEmployeeNotFound() throws Exception {

        Employee request = new Employee();
        request.setDepartment("EMSS");

        mockMvc.perform(put("/api/employees/{id}/department", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }
}

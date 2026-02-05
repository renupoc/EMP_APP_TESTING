package com.example.attendance.integration;

import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Employee;
import com.example.attendance.repository.AttendanceRepository;
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
class AdminAttendanceControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // ===============================
    // GET weekly summary – integration
    // ===============================
    @Test
    void getWeekly_shouldReturnWeeklySummary_fromRealDB() throws Exception {

        // GIVEN – real DB data
        Employee employee = new Employee();
        employee.setFirstName("Renu");
        employee.setLastName("K");
        employee.setEmail("renu@test.com");
        employee.setDepartment("IDIS");

        employee = employeeRepository.save(employee);

        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setMonth(1);
        attendance.setYear(2025);
        attendance.setTotalWorkingDays(20);
        attendance.setWorkedDays(10);

        attendanceRepository.save(attendance);

        // WHEN + THEN
        mockMvc.perform(get("/api/attendance/admin/weekly")
                        .param("employeeId", employee.getId().toString())
                        .param("month", "1")
                        .param("year", "2025"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    // ===============================
    // UPDATE weekly attendance – integration
    // ===============================
    @Test
    void updateWeekly_shouldUpdateAttendance_inRealDB() throws Exception {

        // GIVEN
        Employee employee = new Employee();
        employee.setFirstName("Renu");
        employee.setLastName("K");
        employee.setEmail("renu@test.com");
        employee.setDepartment("VEDC");

        employee = employeeRepository.save(employee);

        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setMonth(1);
        attendance.setYear(2025);
        attendance.setTotalWorkingDays(20);
        attendance.setWorkedDays(5);

        attendanceRepository.save(attendance);

        String requestJson = """
                {
                  "weekNumber": 1,
                  "workedDays": 3
                }
                """;

        // WHEN + THEN
        mockMvc.perform(put("/api/attendance/admin/weekly/update")
                        .param("employeeId", employee.getId().toString())
                        .param("month", "1")
                        .param("year", "2025")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());
    }
}

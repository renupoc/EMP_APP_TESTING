package com.example.attendance.integration;

import com.example.attendance.dto.AttendanceRequest;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Employee;
import com.example.attendance.repository.AttendanceDayRepository;
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

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AttendanceControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private AttendanceDayRepository attendanceDayRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Verifies that submitting valid attendance data
     * returns HTTP 200 and saves attendance successfully.
     */
    @Test
    void submitAttendance_shouldSaveAttendanceAndReturnOk() throws Exception {

        Employee employee = new Employee();
        employee.setFirstName("Renu");
        employee.setLastName("K");
        employee.setEmail("renu@test.com");
        employee.setDepartment("IDIS");

        employee = employeeRepository.save(employee);

        AttendanceRequest request = new AttendanceRequest();
        request.setMonth(1);
        request.setYear(2025);
        request.setTotalDays(31);
        request.setTotalWorkingDays(20);
        request.setWorkedDays(10);
        request.setSelectedDates(List.of("2025-01-02", "2025-01-03"));

        mockMvc.perform(post("/api/attendance/submit/{id}", employee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Attendance saved successfully"));
    }

    /**
     * Verifies validation logic:
     * workedDays must not exceed totalWorkingDays.
     */
    @Test
    void submitAttendance_shouldReturnBadRequest_whenWorkedDaysExceedTotalWorkingDays()
            throws Exception {

        Employee employee = new Employee();
        employee.setFirstName("Renu");
        employee.setLastName("K");
        employee.setEmail("renu@test.com");
        employee.setDepartment("IDIS");

        employee = employeeRepository.save(employee);

        AttendanceRequest request = new AttendanceRequest();
        request.setMonth(1);
        request.setYear(2025);
        request.setTotalDays(31);
        request.setTotalWorkingDays(10);
        request.setWorkedDays(15);

        mockMvc.perform(post("/api/attendance/submit/{id}", employee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Worked days cannot exceed total working days"));
    }

    /**
     * Fetches attendance records for a specific employee
     * and validates the returned data.
     */
    @Test
    void getAttendanceByEmployee_shouldReturnAttendanceList() throws Exception {

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
        attendance.setTotalDays(31);
        attendance.setTotalWorkingDays(20);
        attendance.setWorkedDays(10);

        attendanceRepository.save(attendance);

        mockMvc.perform(get("/api/attendance/employee/{id}", employee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].workedDays").value(10));
    }

    /**
     * Retrieves all attendance records as an admin
     * and expects HTTP 200 OK.
     */
    @Test
    void getAllAttendance_shouldReturnAllRecords() throws Exception {

        mockMvc.perform(get("/api/attendance/admin/all"))
                .andExpect(status().isOk());
    }

    /**
     * Deletes an attendance record by ID
     * and verifies the operation succeeds.
     */
    @Test
    void deleteAttendance_shouldDeleteRecord() throws Exception {

        Employee employee = new Employee();
        employee.setFirstName("Renu");
        employee.setLastName("K");
        employee.setEmail("renu@test.com");
        employee.setDepartment("EMSS");

        employee = employeeRepository.save(employee);

        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setMonth(1);
        attendance.setYear(2025);
        attendance.setTotalWorkingDays(20);
        attendance.setWorkedDays(5);

        attendance = attendanceRepository.save(attendance);

        mockMvc.perform(delete("/api/attendance/admin/{id}", attendance.getId()))
                .andExpect(status().isOk());
    }

    /**
     * Updates worked days and department for an attendance record
     * and validates HTTP 200 response.
     */
    @Test
    void updateAttendance_shouldUpdateWorkedDaysAndDepartment() throws Exception {

        Employee employee = new Employee();
        employee.setFirstName("Renu");
        employee.setLastName("K");
        employee.setEmail("renu@test.com");
        employee.setDepartment("GUSS");

        employee = employeeRepository.save(employee);

        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setMonth(1);
        attendance.setYear(2025);
        attendance.setTotalWorkingDays(20);
        attendance.setWorkedDays(5);

        attendance = attendanceRepository.save(attendance);

        Map<String, Object> payload = Map.of(
                "workedDays", 15,
                "department", "IDIS"
        );

        mockMvc.perform(put("/api/attendance/admin/update/{id}", attendance.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());
    }
}

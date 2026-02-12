package com.example.attendance.controller;

import com.example.attendance.dto.AttendanceRequest;
import com.example.attendance.entity.Employee;
import com.example.attendance.repository.AttendanceDayRepository;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.service.AttendanceService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AttendanceController.class)
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Mock all constructor dependencies of AttendanceController
    @MockBean
    private AttendanceRepository attendanceRepository;

    @MockBean
    private AttendanceDayRepository attendanceDayRepository;

    @MockBean
    private EmployeeRepository employeeRepository;

    @MockBean
    private AttendanceService attendanceService;

    /**
     * Verifies that the admin endpoint
     * GET /api/attendance/admin/all
     * returns HTTP 200 OK.
     */
    @Test
    void getAllAttendance_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/attendance/admin/all"))
                .andExpect(status().isOk());
    }

    /**
     * Submits valid attendance data
     * and expects HTTP 200 OK.
     */
    @Test
    void submitAttendance_shouldReturn200_whenRequestIsValid() throws Exception {

        Long employeeId = 1L;

        Employee employee = new Employee();
        employee.setId(employeeId);

        AttendanceRequest request = new AttendanceRequest();
        request.setMonth(1);
        request.setYear(2025);
        request.setTotalDays(31);
        request.setTotalWorkingDays(20);
        request.setWorkedDays(10);

        when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.of(employee));

        when(attendanceRepository
                .findByEmployeeIdAndMonthAndYear(employeeId, 1, 2025))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                post("/api/attendance/submit/{employeeId}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isOk());

        verify(attendanceRepository, times(1)).save(any());
    }

    /**
     * Returns 400 BAD REQUEST when
     * totalWorkingDays is zero.
     */
    @Test
    void submitAttendance_shouldReturn400_whenTotalWorkingDaysIsZero() throws Exception {

        Long employeeId = 1L;

        Employee employee = new Employee();
        employee.setId(employeeId);

        AttendanceRequest request = new AttendanceRequest();
        request.setMonth(1);
        request.setYear(2025);
        request.setTotalDays(31);
        request.setTotalWorkingDays(0);
        request.setWorkedDays(0);

        when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.of(employee));

        mockMvc.perform(
                post("/api/attendance/submit/{employeeId}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isBadRequest());

        verify(attendanceRepository, never()).save(any());
    }

    /**
     * Returns 400 BAD REQUEST when
     * workedDays exceed totalWorkingDays.
     */
    @Test
    void submitAttendance_shouldReturn400_whenWorkedDaysExceedTotalWorkingDays() throws Exception {

        Long employeeId = 1L;

        Employee employee = new Employee();
        employee.setId(employeeId);

        AttendanceRequest request = new AttendanceRequest();
        request.setMonth(1);
        request.setYear(2025);
        request.setTotalDays(31);
        request.setTotalWorkingDays(10);
        request.setWorkedDays(15);

        when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.of(employee));

        mockMvc.perform(
                post("/api/attendance/submit/{employeeId}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isBadRequest());

        verify(attendanceRepository, never()).save(any());
    }

    /**
     * Returns 500 INTERNAL SERVER ERROR
     * when employee does not exist.
     */
    @Test
    void submitAttendance_shouldReturn500_whenEmployeeNotFound() throws Exception {

        Long employeeId = 99L;

        AttendanceRequest request = new AttendanceRequest();
        request.setMonth(1);
        request.setYear(2025);
        request.setTotalDays(31);
        request.setTotalWorkingDays(20);
        request.setWorkedDays(10);

        when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                post("/api/attendance/submit/{employeeId}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.message").value("Employee not found"))
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

    /**
     * Returns 400 BAD REQUEST when
     * totalWorkingDays exceed totalDays.
     */
    @Test
    void saveAttendance_shouldReturnBadRequest_whenWorkingDaysExceedTotalDays() throws Exception {

        Employee employee = new Employee();
        employee.setId(1L);

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        String payload = """
            {
              "month": 1,
              "year": 2026,
              "totalDays": 20,
              "totalWorkingDays": 25,
              "workedDays": 10
            }
            """;

        mockMvc.perform(
                post("/api/attendance/submit/{employeeId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message")
                .value("Working days cannot exceed total days"));
    }
}

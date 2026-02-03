package com.example.attendance.controller;

import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.dto.AttendanceRequest;
import com.example.attendance.entity.Employee;
import com.example.attendance.repository.AttendanceDayRepository;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.service.AttendanceService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(AttendanceController.class)
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
private ObjectMapper objectMapper;

    // 🔴 MUST mock ALL constructor dependencies
    @MockBean
    private AttendanceRepository attendanceRepository;

    @MockBean
    private AttendanceDayRepository attendanceDayRepository;

    @MockBean
    private EmployeeRepository employeeRepository;

    @MockBean
    private AttendanceService attendanceService;

    @Test
    void getAllAttendance_shouldReturn200() throws Exception {
        mockMvc.perform(
                get("/api/attendance/admin/all")
        ).andExpect(status().isOk());
    }

    //POST controller test (/submit/{employeeId})
@Test
void submitAttendance_shouldReturn200_whenRequestIsValid() throws Exception {

    // GIVEN
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

    // WHEN + THEN
    mockMvc.perform(
            post("/api/attendance/submit/{employeeId}", employeeId)
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
    )
    .andExpect(status().isOk());

    // VERIFY persistence happened
    verify(attendanceRepository, times(1)).save(any());
}

// =====================
    // 🔽 ADD THESE HERE 🔽
    // VALIDATION TESTS (400)
    // =====================

//TEST 1: totalWorkingDays = 0 → 400 BAD REQUEST

@Test
void submitAttendance_shouldReturn400_whenTotalWorkingDaysIsZero() throws Exception {

    Long employeeId = 1L;

    Employee employee = new Employee();
    employee.setId(employeeId);

    AttendanceRequest request = new AttendanceRequest();
    request.setMonth(1);
    request.setYear(2025);
    request.setTotalDays(31);
    request.setTotalWorkingDays(0);   // ❌ INVALID
    request.setWorkedDays(0);

    when(employeeRepository.findById(employeeId))
            .thenReturn(Optional.of(employee));

    mockMvc.perform(
            post("/api/attendance/submit/{employeeId}", employeeId)
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
    )
    .andExpect(status().isBadRequest());

    // Ensure nothing is saved
    verify(attendanceRepository, never()).save(any());
}

//🧪 TEST 2: workedDays > totalWorkingDays → 400 BAD REQUEST

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
    request.setWorkedDays(15);   // ❌ INVALID

    when(employeeRepository.findById(employeeId))
            .thenReturn(Optional.of(employee));

    mockMvc.perform(
            post("/api/attendance/submit/{employeeId}", employeeId)
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
    )
    .andExpect(status().isBadRequest());

    verify(attendanceRepository, never()).save(any());
}

//
@Test
void submitAttendance_shouldReturn500_withErrorJson_whenEmployeeNotFound() throws Exception {

    Long employeeId = 99L;

    AttendanceRequest request = new AttendanceRequest();
    request.setMonth(1);
    request.setYear(2025);
    request.setTotalDays(31);
    request.setTotalWorkingDays(20);
    request.setWorkedDays(10);

    // Employee NOT found
    when(employeeRepository.findById(employeeId))
            .thenReturn(Optional.empty());

    mockMvc.perform(
            post("/api/attendance/submit/{employeeId}", employeeId)
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
    )
    .andExpect(status().isInternalServerError())
    .andExpect(jsonPath("$.message").value("Employee not found"))
    .andExpect(jsonPath("$.status").value(500))
    .andExpect(jsonPath("$.error").value("Internal Server Error"));
}



}

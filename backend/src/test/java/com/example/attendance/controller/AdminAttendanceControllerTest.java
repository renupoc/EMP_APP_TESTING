package com.example.attendance.controller;

import com.example.attendance.dto.WeeklySummaryResponse;
import com.example.attendance.dto.WeeklyUpdateRequest;
import com.example.attendance.service.AttendanceService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminAttendanceController.class)
class AdminAttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttendanceService attendanceService;

    @Autowired
    private ObjectMapper objectMapper;

    // ===============================
    // GET weekly summary
    // ===============================
    @Test
    void getWeekly_shouldReturnWeeklySummary() throws Exception {

        WeeklySummaryResponse response = new WeeklySummaryResponse();
        response.setWeekNumber(1);
        response.setStart(LocalDate.of(2025, 1, 1));
        response.setEnd(LocalDate.of(2025, 1, 7));
        response.setTotalWorkingDays(5);
        response.setWorkedDays(4);
        response.setAvailability(80);

        when(attendanceService.getWeeklyAttendance(1L, 1, 2025))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/attendance/admin/weekly")
                        .param("employeeId", "1")
                        .param("month", "1")
                        .param("year", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].weekNumber").value(1))
                .andExpect(jsonPath("$[0].workedDays").value(4))
                .andExpect(jsonPath("$[0].availability").value(80));

        verify(attendanceService)
                .getWeeklyAttendance(1L, 1, 2025);
    }

    // ===============================
    // UPDATE weekly attendance
    // ===============================
    @Test
    void updateWeekly_shouldCallServiceAndReturnOk() throws Exception {

        WeeklyUpdateRequest request = new WeeklyUpdateRequest();
        request.setWeekNumber(1);
        request.setWorkedDays(3);

        mockMvc.perform(put("/api/attendance/admin/weekly/update")
                        .param("employeeId", "1")
                        .param("month", "1")
                        .param("year", "2025")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(attendanceService).updateWeeklyAttendance(
                1L,
                1,
                2025,
                1,
                3
        );
    }
}

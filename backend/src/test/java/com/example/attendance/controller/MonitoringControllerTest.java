package com.example.attendance.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MonitoringController.class)
class MonitoringControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ===============================
    // GET /hello
    // ===============================
    @Test
    void hello_shouldReturnRunningMessage() throws Exception {

        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Employee Attendance Backend is running"
                ));
    }

    // ===============================
    // GET /health
    // ===============================
    @Test
    void health_shouldReturnServiceStatus() throws Exception {

        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service")
                        .value("employee-attendance-backend"));
    }
}

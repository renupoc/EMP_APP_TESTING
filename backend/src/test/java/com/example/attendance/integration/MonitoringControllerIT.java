package com.example.attendance.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MonitoringControllerIT {

    @Autowired
    private MockMvc mockMvc;

    // ===============================
    // GET /hello
    // ===============================
    @Test
    void hello_shouldReturnApplicationStatusMessage() throws Exception {

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
    void health_shouldReturnHealthStatusMap() throws Exception {

        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service")
                        .value("employee-attendance-backend"));
    }
}

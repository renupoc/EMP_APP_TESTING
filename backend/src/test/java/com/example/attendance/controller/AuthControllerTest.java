package com.example.attendance.controller;

import com.example.attendance.dto.LoginRequest;
import com.example.attendance.dto.RegisterRequest;
import com.example.attendance.entity.Employee;
import com.example.attendance.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    // ===============================
    // REGISTER
    // ===============================
    @Test
    void register_shouldReturnEmployee_whenRequestValid() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Renu");
        request.setLastName("K");
        request.setEmail("renu@test.com");
        request.setPassword("1234");
        request.setDepartment("IDIS");

        Employee saved = new Employee();
        saved.setId(1L);
        saved.setFirstName("Renu");
        saved.setLastName("K");
        saved.setEmail("renu@test.com");
        saved.setDepartment("IDIS");
        saved.setRole("EMPLOYEE");

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(saved);

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk());
    }

    // ===============================
    // LOGIN
    // ===============================
    @Test
    void login_shouldReturnResponseMap_whenCredentialsValid() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setEmail("renu@test.com");
        request.setPassword("1234");

        Map<String, Object> response = new HashMap<>();
        response.put("employeeId", 1L);
        response.put("fullName", "Renu K");
        response.put("email", "renu@test.com");
        response.put("department", "IDIS");
        response.put("role", "EMPLOYEE");

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk());
    }

@Test
void login_shouldReturn500_whenInvalidCredentials() throws Exception {
    LoginRequest request = new LoginRequest();
    request.setEmail("wrong@test.com");
    request.setPassword("wrong");

    when(authService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Invalid credentials"));

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isInternalServerError());
}
@Test
void register_shouldReturn500_whenEmailAlreadyExists() throws Exception {
    RegisterRequest request = new RegisterRequest();
    request.setEmail("renu@test.com");

    when(authService.register(any(RegisterRequest.class)))
            .thenThrow(new RuntimeException("Email already registered"));

    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isInternalServerError());
}

}

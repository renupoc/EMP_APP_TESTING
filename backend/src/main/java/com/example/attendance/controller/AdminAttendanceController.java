package com.example.attendance.controller;

import com.example.attendance.dto.WeeklySummaryResponse;
import com.example.attendance.dto.WeeklyUpdateRequest;
import com.example.attendance.service.AttendanceService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance/admin")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminAttendanceController {

    private final AttendanceService attendanceService;

    public AdminAttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    // ===============================
    // GET weekly summary
    // ===============================
    @GetMapping("/weekly")
    public List<WeeklySummaryResponse> getWeekly(
            @RequestParam Long employeeId,
            @RequestParam int month,
            @RequestParam int year
    ) {
        return attendanceService.getWeeklyAttendance(employeeId, month, year);
    }

    // ===============================
    // UPDATE weekly attendance
    // ===============================
    @PutMapping("/weekly/update")
    public void updateWeekly(
            @RequestParam Long employeeId,
            @RequestParam int month,
            @RequestParam int year,
            @RequestBody WeeklyUpdateRequest request
    ) {
        attendanceService.updateWeeklyAttendance(
                employeeId,
                month,
                year,
                request.getWeekNumber(),
                request.getWorkedDays()
        );
    }
}
package com.example.attendance.integration;

import com.example.attendance.dto.AttendanceRequest;
import com.example.attendance.dto.WeeklySummaryResponse;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Employee;
import com.example.attendance.repository.AttendanceDayRepository;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.AttendanceWeeklyRepository;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.service.AttendanceService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AttendanceServiceIT {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private AttendanceDayRepository attendanceDayRepository;

    @Autowired
    private AttendanceWeeklyRepository attendanceWeeklyRepository;

    // =====================================================
    // submitAttendance – integration
    // =====================================================
    @Test
    void submitAttendance_shouldPersistAttendanceInDatabase() {

        // GIVEN
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

        // WHEN
        attendanceService.submitAttendance(employee.getId(), request);

        // THEN
        Attendance saved = attendanceRepository
                .findByEmployeeIdAndMonthAndYear(employee.getId(), 1, 2025)
                .orElse(null);

        assertNotNull(saved);
        assertEquals(10, saved.getWorkedDays());
        assertEquals(50, saved.getAvailability()); // 10/20 * 100
    }

    // =====================================================
    // updateWeeklyAttendance – integration
    // =====================================================
    @Test
    void updateWeeklyAttendance_shouldCreateDailyAttendanceAndRecalculateMonthly() {

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
        attendance.setWorkedDays(0);

        attendanceRepository.save(attendance);

        // WHEN
        attendanceService.updateWeeklyAttendance(
                employee.getId(),
                1,
                2025,
                1,   // week 1
                3    // worked days
        );

        // THEN – daily attendance created
        assertTrue(attendanceDayRepository
                .findByEmployee_IdAndDateBetween(
                        employee.getId(),
                        java.time.LocalDate.of(2025, 1, 1),
                        java.time.LocalDate.of(2025, 1, 7)
                ).size() > 0);

        Attendance updated = attendanceRepository
                .findByEmployeeIdAndMonthAndYear(employee.getId(), 1, 2025)
                .orElseThrow();

        assertTrue(updated.getWorkedDays() > 0);
    }

    // =====================================================
    // getWeeklyAttendance – integration
    // =====================================================
    @Test
    void getWeeklyAttendance_shouldReturnWeeklySummaryAndPersistWeeklyData() {

        // GIVEN
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
        attendance.setWorkedDays(0);

        attendanceRepository.save(attendance);

        // WHEN
        List<WeeklySummaryResponse> result =
                attendanceService.getWeeklyAttendance(
                        employee.getId(),
                        1,
                        2025
                );

        // THEN
        assertNotNull(result);
        assertFalse(result.isEmpty());

        assertTrue(attendanceWeeklyRepository.count() > 0);

        Attendance updated = attendanceRepository
                .findByEmployeeIdAndMonthAndYear(employee.getId(), 1, 2025)
                .orElseThrow();

        assertTrue(updated.getAvailability() >= 0);
    }

    // =====================================================
    // NEGATIVE – employee not found
    // =====================================================
    @Test
    void submitAttendance_employeeNotFound_shouldThrowException() {

        AttendanceRequest request = new AttendanceRequest();
        request.setMonth(1);
        request.setYear(2025);
        request.setTotalDays(31);
        request.setTotalWorkingDays(20);
        request.setWorkedDays(5);

        assertThrows(RuntimeException.class, () ->
                attendanceService.submitAttendance(999L, request)
        );
    }
}

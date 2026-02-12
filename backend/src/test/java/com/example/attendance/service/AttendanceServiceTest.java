package com.example.attendance.service;

import com.example.attendance.dto.AttendanceRequest;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.AttendanceDay;
import com.example.attendance.entity.Employee;
import com.example.attendance.repository.AttendanceDayRepository;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.AttendanceWeeklyRepository;
import com.example.attendance.repository.EmployeeRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    AttendanceRepository attendanceRepository;

    @Mock
    EmployeeRepository employeeRepository;

    @Mock
    AttendanceDayRepository attendanceDayRepository;

    @Mock
    AttendanceWeeklyRepository attendanceWeeklyRepository;

    @InjectMocks
    AttendanceService attendanceService;

    @Test
    void submitAttendance_shouldSaveAttendance() {

        Employee employee = new Employee();
        employee.setId(1L);

        AttendanceRequest request = new AttendanceRequest();
        request.setMonth(1);
        request.setYear(2025);
        request.setTotalDays(31);
        request.setTotalWorkingDays(20);
        request.setWorkedDays(10);

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        when(attendanceRepository
                .findByEmployeeIdAndMonthAndYear(1L, 1, 2025))
                .thenReturn(Optional.empty());

        attendanceService.submitAttendance(1L, request);

        verify(attendanceRepository, times(1))
                .save(any(Attendance.class));
    }

    @Test
    void submitAttendance_employeeNotFound_shouldThrowException() {

    AttendanceRequest request = new AttendanceRequest();
    request.setMonth(1);
    request.setYear(2025);
    request.setTotalDays(31);
    request.setTotalWorkingDays(20);
    request.setWorkedDays(10);

    // Mock: employee NOT found
    when(employeeRepository.findById(1L))
            .thenReturn(Optional.empty());

    // Assert: exception is thrown
    assertThrows(RuntimeException.class, () ->
            attendanceService.submitAttendance(1L, request)
    );

    // Verify: attendance is NOT saved
    verify(attendanceRepository, never()).save(any());
}

//availability assertion test
@Test
void submitAttendance_shouldCalculateAvailabilityCorrectly() {

    Employee employee = new Employee();
    employee.setId(1L);

    AttendanceRequest request = new AttendanceRequest();
    request.setMonth(1);
    request.setYear(2025);
    request.setTotalDays(31);
    request.setTotalWorkingDays(20);
    request.setWorkedDays(10); // 50%

    when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(employee));

    when(attendanceRepository
            .findByEmployeeIdAndMonthAndYear(1L, 1, 2025))
            .thenReturn(Optional.empty());

    // Capture Attendance passed to save()
    ArgumentCaptor<Attendance> captor =
            ArgumentCaptor.forClass(Attendance.class);

    attendanceService.submitAttendance(1L, request);

    verify(attendanceRepository).save(captor.capture());

    Attendance savedAttendance = captor.getValue();

    assertEquals(50, savedAttendance.getAvailability());
}

//Add the EDGE CASE test

@Test
void submitAttendance_whenTotalWorkingDaysIsZero_shouldSetAvailabilityToZero() {

    Employee employee = new Employee();
    employee.setId(1L);

    AttendanceRequest request = new AttendanceRequest();
    request.setMonth(1);
    request.setYear(2025);
    request.setTotalDays(31);
    request.setTotalWorkingDays(0); // EDGE CASE
    request.setWorkedDays(10);      // even if workedDays > 0

    when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(employee));

    when(attendanceRepository
            .findByEmployeeIdAndMonthAndYear(1L, 1, 2025))
            .thenReturn(Optional.empty());

    ArgumentCaptor<Attendance> captor =
            ArgumentCaptor.forClass(Attendance.class);

    attendanceService.submitAttendance(1L, request);

    verify(attendanceRepository).save(captor.capture());

    Attendance savedAttendance = captor.getValue();

    // ✅ ASSERT EDGE CASE BEHAVIOR
    assertEquals(0, savedAttendance.getAvailability());
}

//Unit test updateWeeklyAttendance()

@Test
void updateWeeklyAttendance_shouldUpdateAttendanceForValidWeek() {

    // GIVEN
    Employee employee = new Employee();
    employee.setId(1L);

    Attendance attendance = new Attendance();
    attendance.setTotalWorkingDays(20);

    when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(employee));

    when(attendanceRepository
            .findByEmployeeIdAndMonthAndYear(1L, 1, 2025))
            .thenReturn(Optional.of(attendance));

    when(attendanceDayRepository
            .findByEmployee_IdAndDateBetween(any(), any(), any()))
            .thenReturn(
                    java.util.List.of(
                            new AttendanceDay(),
                            new AttendanceDay()
                    )
            );

    // WHEN
    attendanceService.updateWeeklyAttendance(
            1L,
            1,
            2025,
            1,
            3
    );

    // THEN
    verify(attendanceDayRepository, atLeastOnce())
            .save(any(AttendanceDay.class));

    verify(attendanceRepository, atLeastOnce())
            .save(attendance);
}

    @Test
void updateWeeklyAttendance_invalidWeek_shouldThrowException() {

    Employee employee = new Employee();
    employee.setId(1L);

    when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(employee));

    assertThrows(RuntimeException.class, () ->
            attendanceService.updateWeeklyAttendance(
                    1L,
                    1,
                    2025,
                    10,  // INVALID week
                    2
            )
    );

    verify(attendanceDayRepository, never()).save(any());
    verify(attendanceRepository, never()).save(any());
}

//Unit test getWeeklyAttendance()

@Test
void getWeeklyAttendance_shouldReturnWeeklySummary() {

    // GIVEN
    Employee employee = new Employee();
    employee.setId(1L);

    Attendance attendance = new Attendance();
    attendance.setTotalWorkingDays(20);

    when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(employee));

    when(attendanceRepository
            .findByEmployeeIdAndMonthAndYear(1L, 1, 2025))
            .thenReturn(Optional.of(attendance));

    // Assume employee was present on working days
    when(attendanceDayRepository
            .existsByEmployee_IdAndDate(anyLong(), any()))
            .thenReturn(true);

    // Used in monthly recalculation
    when(attendanceDayRepository
            .findByEmployee_IdAndDateBetween(any(), any(), any()))
            .thenReturn(
                    java.util.List.of(
                            new AttendanceDay(),
                            new AttendanceDay()
                    )
            );

    // WHEN
    var result = attendanceService.getWeeklyAttendance(1L, 1, 2025);

    // THEN
    assertNotNull(result);
    assertFalse(result.isEmpty());

    // Weekly attendance saved
    verify(attendanceWeeklyRepository, atLeastOnce())
            .save(any());

    // Monthly attendance recalculated & saved
    verify(attendanceRepository, atLeastOnce())
            .save(attendance);
}

//Exception tests for getWeeklyAttendance()

@Test
void getWeeklyAttendance_employeeNotFound_shouldThrowException() {

    // GIVEN: employee does NOT exist
    when(employeeRepository.findById(1L))
            .thenReturn(Optional.empty());

    // WHEN + THEN
    assertThrows(RuntimeException.class, () ->
            attendanceService.getWeeklyAttendance(1L, 1, 2025)
    );

    // Verify: no DB write happens
    verify(attendanceWeeklyRepository, never()).save(any());
    verify(attendanceRepository, never()).save(any());
}

@Test
void getWeeklyAttendance_attendanceNotFound_shouldThrowException() {

    Employee employee = new Employee();
    employee.setId(1L);

    // Employee exists
    when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(employee));

    // Attendance does NOT exist
    when(attendanceRepository
            .findByEmployeeIdAndMonthAndYear(1L, 1, 2025))
            .thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () ->
            attendanceService.getWeeklyAttendance(1L, 1, 2025)
    );

    verify(attendanceWeeklyRepository, never()).save(any());
    verify(attendanceRepository, never()).save(any());
}

@Test
void updateWeeklyAttendance_shouldThrowException_whenWeekInvalid() {
    RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> attendanceService.updateWeeklyAttendance(
                    1L, 1, 2024, 0, 5   // ❌ invalid week
            )
    );

    assertNotNull(ex);
}



}

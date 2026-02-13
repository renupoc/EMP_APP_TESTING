package com.example.attendance.service;

import com.example.attendance.dto.AttendanceRequest;
import com.example.attendance.dto.WeeklySummaryResponse;
import com.example.attendance.entity.*;
import com.example.attendance.repository.*;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceDayRepository attendanceDayRepository;
    private final AttendanceWeeklyRepository attendanceWeeklyRepository;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            EmployeeRepository employeeRepository,
            AttendanceDayRepository attendanceDayRepository,
            AttendanceWeeklyRepository attendanceWeeklyRepository
    ) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.attendanceDayRepository = attendanceDayRepository;
        this.attendanceWeeklyRepository = attendanceWeeklyRepository;
    }

    // =====================================================
    // EMPLOYEE – SUBMIT MONTHLY ATTENDANCE
    // =====================================================
    public void submitAttendance(Long employeeId, AttendanceRequest request) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndMonthAndYear(
                        employeeId,
                        request.getMonth(),
                        request.getYear()
                )
                .orElse(new Attendance());

        attendance.setEmployee(employee);
        attendance.setMonth(request.getMonth());
        attendance.setYear(request.getYear());
        attendance.setTotalDays(request.getTotalDays());
        attendance.setTotalWorkingDays(request.getTotalWorkingDays());
        attendance.setWorkedDays(request.getWorkedDays());

        int availability =
                request.getTotalWorkingDays() == 0 ? 0 :
                        (request.getWorkedDays() * 100) / request.getTotalWorkingDays();

        attendance.setAvailability(availability);

        attendanceRepository.save(attendance);
    }

    // =====================================================
    // ADMIN – UPDATE WEEKLY ATTENDANCE
    // =====================================================
    public void updateWeeklyAttendance(
            Long employeeId,
            int month,
            int year,
            int weekNumber,
            int workedDays
    ) {

        YearMonth ym = YearMonth.of(year, month);
        int totalDays = ym.lengthOfMonth();

        int currentWeek = 1;
        int weekStartDay = 1;

        Employee employee = employeeRepository
                .findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        while (weekStartDay <= totalDays) {

            LocalDate startDate = LocalDate.of(year, month, weekStartDay);
            LocalDate endDate = startDate;

            while (endDate.getDayOfWeek() != DayOfWeek.SUNDAY
                    && endDate.getDayOfMonth() < totalDays) {
                endDate = endDate.plusDays(1);
            }

            if (currentWeek == weekNumber) {

                attendanceDayRepository
                        .deleteByEmployee_IdAndDateBetween(employeeId, startDate, endDate);

                int count = 0;
                LocalDate cursor = startDate;

                while (!cursor.isAfter(endDate) && count < workedDays) {
                    if (cursor.getDayOfWeek() != DayOfWeek.SATURDAY
                            && cursor.getDayOfWeek() != DayOfWeek.SUNDAY) {

                        AttendanceDay day = new AttendanceDay();
                        day.setEmployee(employee);
                        day.setDate(cursor);
                        day.setStatus("PRESENT");
                        attendanceDayRepository.save(day);
                        count++;
                    }
                    cursor = cursor.plusDays(1);
                }

                Attendance attendance = attendanceRepository
                        .findByEmployeeIdAndMonthAndYear(employeeId, month, year)
                        .orElseThrow(() -> new RuntimeException("Attendance not found"));

                recalculateMonthlyAvailability(attendance, employeeId, month, year);
                return;
            }

            currentWeek++;
            weekStartDay = endDate.getDayOfMonth() + 1;
        }

        throw new RuntimeException("Invalid week number");
    }

    // =====================================================
    // ADMIN – WEEKLY SUMMARY
    // =====================================================
    public List<WeeklySummaryResponse> getWeeklyAttendance(
            Long employeeId, int month, int year) {

        List<WeeklySummaryResponse> result = new ArrayList<>();

        YearMonth ym = YearMonth.of(year, month);
        int totalDays = ym.lengthOfMonth();

        int weekNumber = 1;
        int weekStartDay = 1;

        Employee employee = employeeRepository
                .findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndMonthAndYear(employeeId, month, year)
                .orElseThrow(() -> new RuntimeException("Attendance not found"));

        while (weekStartDay <= totalDays) {

            LocalDate startDate = LocalDate.of(year, month, weekStartDay);
            LocalDate endDate = startDate;

            while (endDate.getDayOfWeek() != DayOfWeek.SUNDAY
                    && endDate.getDayOfMonth() < totalDays) {
                endDate = endDate.plusDays(1);
            }

            int totalWorkingDays = 0;
            int workedDays = 0;

            LocalDate temp = startDate;
            while (!temp.isAfter(endDate)) {

                if (temp.getDayOfWeek() != DayOfWeek.SATURDAY
                        && temp.getDayOfWeek() != DayOfWeek.SUNDAY) {

                    totalWorkingDays++;

                    if (attendanceDayRepository
                            .existsByEmployee_IdAndDate(employeeId, temp)) {
                        workedDays++;
                    }
                }
                temp = temp.plusDays(1);
            }

            AttendanceWeekly weekly = attendanceWeeklyRepository
                    .findByAttendance_IdAndYearAndMonthAndWeekNumber(
                            attendance.getId(), year, month, weekNumber
                    )
                    .orElse(new AttendanceWeekly());

            weekly.setEmployee(employee);
            weekly.setAttendance(attendance);
            weekly.setYear(year);
            weekly.setMonth(month);
            weekly.setWeekNumber(weekNumber);
            weekly.setWeekStart(startDate);
            weekly.setWeekEnd(endDate);
            weekly.setTotalWorkingDays(totalWorkingDays);
            weekly.setWorkedDays(workedDays);
            weekly.setAvailability(
                    totalWorkingDays == 0 ? 0 :
                            (workedDays * 100) / totalWorkingDays
            );

            attendanceWeeklyRepository.save(weekly);

            WeeklySummaryResponse w = new WeeklySummaryResponse();
            w.setWeekNumber(weekNumber++);
            w.setStart(startDate);
            w.setEnd(endDate);
            w.setTotalWorkingDays(totalWorkingDays);
            w.setWorkedDays(workedDays);
            w.setAvailability(weekly.getAvailability());

            result.add(w);
            weekStartDay = endDate.getDayOfMonth() + 1;
        }

        // ⭐ FIX: ALWAYS UPDATE MONTHLY AVAILABILITY
        recalculateMonthlyAvailability(attendance, employeeId, month, year);

        return result;
    }

    // =====================================================
    // MONTHLY AVAILABILITY RECALCULATION
    // =====================================================
    private void recalculateMonthlyAvailability(
            Attendance attendance,
            Long employeeId,
            int month,
            int year
    ) {
        YearMonth ym = YearMonth.of(year, month);

        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        int workedDays =
                attendanceDayRepository
                        .findByEmployee_IdAndDateBetween(employeeId, start, end)
                        .size();

        int totalWorkingDays = attendance.getTotalWorkingDays();

        int availability =
                totalWorkingDays == 0 ? 0 :
                        (workedDays * 100) / totalWorkingDays;

        attendance.setWorkedDays(workedDays);
        attendance.setAvailability(availability);

        attendanceRepository.save(attendance);
    }
}

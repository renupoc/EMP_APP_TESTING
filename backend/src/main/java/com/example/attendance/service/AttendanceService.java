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

    /* =========================
       Sonar-safe constants
       ========================= */
    private static final String EMPLOYEE_NOT_FOUND = "Employee not found";
    private static final String ATTENDANCE_NOT_FOUND = "Attendance not found";
    private static final String INVALID_WEEK_NUMBER = "Invalid week number";

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

        Employee employee = getEmployee(employeeId);

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

        Employee employee = getEmployee(employeeId);
        Attendance attendance = getAttendance(employeeId, month, year);

        YearMonth ym = YearMonth.of(year, month);
        int totalDays = ym.lengthOfMonth();

        int currentWeek = 1;
        int weekStartDay = 1;

        while (weekStartDay <= totalDays) {

            WeekRange range =
                    calculateWeekRange(year, month, weekStartDay, totalDays);

            if (currentWeek == weekNumber) {

                attendanceDayRepository
                        .deleteByEmployee_IdAndDateBetween(
                                employeeId,
                                range.start(),
                                range.end()
                        );

                saveWorkedDays(employee, range, workedDays);

                recalculateMonthlyAvailability(
                        attendance, employeeId, month, year);
                return;
            }

            currentWeek++;
            weekStartDay = range.end().getDayOfMonth() + 1;
        }

        throw new IllegalArgumentException(INVALID_WEEK_NUMBER);
    }

    // =====================================================
    // ADMIN – WEEKLY SUMMARY
    // =====================================================
    public List<WeeklySummaryResponse> getWeeklyAttendance(
            Long employeeId, int month, int year) {

        Employee employee = getEmployee(employeeId);
        Attendance attendance = getAttendance(employeeId, month, year);

        YearMonth ym = YearMonth.of(year, month);
        int totalDays = ym.lengthOfMonth();

        List<WeeklySummaryResponse> result = new ArrayList<>();

        int weekNumber = 1;
        int weekStartDay = 1;

        while (weekStartDay <= totalDays) {

            WeekRange range =
                    calculateWeekRange(year, month, weekStartDay, totalDays);

            WeeklyCounts counts =
                    calculateWeeklyCounts(employeeId, range.start(), range.end());

            AttendanceWeekly weekly =
                    upsertWeeklyAttendance(
                            employee, attendance, year, month, weekNumber, range, counts);

            result.add(
                    toWeeklyResponse(weekly, weekNumber++, range, counts));

            weekStartDay = range.end().getDayOfMonth() + 1;
        }

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

    // =====================================================
    // PRIVATE HELPERS
    // =====================================================

    private Employee getEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new IllegalArgumentException(EMPLOYEE_NOT_FOUND));
    }

    private Attendance getAttendance(Long employeeId, int month, int year) {
        return attendanceRepository
                .findByEmployeeIdAndMonthAndYear(employeeId, month, year)
                .orElseThrow(() ->
                        new IllegalArgumentException(ATTENDANCE_NOT_FOUND));
    }

    private WeekRange calculateWeekRange(
            int year, int month, int startDay, int totalDays) {

        LocalDate start = LocalDate.of(year, month, startDay);
        LocalDate end = start;

        while (end.getDayOfWeek() != DayOfWeek.SUNDAY
                && end.getDayOfMonth() < totalDays) {
            end = end.plusDays(1);
        }
        return new WeekRange(start, end);
    }

    private WeeklyCounts calculateWeeklyCounts(
            Long employeeId, LocalDate start, LocalDate end) {

        int totalWorkingDays = 0;
        int workedDays = 0;

        LocalDate date = start;
        while (!date.isAfter(end)) {

            if (!isWeekend(date)) {
                totalWorkingDays++;
                if (attendanceDayRepository
                        .existsByEmployee_IdAndDate(employeeId, date)) {
                    workedDays++;
                }
            }
            date = date.plusDays(1);
        }
        return new WeeklyCounts(totalWorkingDays, workedDays);
    }

    private void saveWorkedDays(
            Employee employee, WeekRange range, int workedDays) {

        int count = 0;
        LocalDate date = range.start();

        while (!date.isAfter(range.end()) && count < workedDays) {
            if (!isWeekend(date)) {

                AttendanceDay day = new AttendanceDay();
                day.setEmployee(employee);
                day.setDate(date);
                day.setStatus("PRESENT");
                attendanceDayRepository.save(day);
                count++;
            }
            date = date.plusDays(1);
        }
    }

    private AttendanceWeekly upsertWeeklyAttendance(
            Employee employee,
            Attendance attendance,
            int year,
            int month,
            int weekNumber,
            WeekRange range,
            WeeklyCounts counts
    ) {

        AttendanceWeekly weekly =
                attendanceWeeklyRepository
                        .findByAttendance_IdAndYearAndMonthAndWeekNumber(
                                attendance.getId(), year, month, weekNumber)
                        .orElse(new AttendanceWeekly());

        weekly.setEmployee(employee);
        weekly.setAttendance(attendance);
        weekly.setYear(year);
        weekly.setMonth(month);
        weekly.setWeekNumber(weekNumber);
        weekly.setWeekStart(range.start());
        weekly.setWeekEnd(range.end());
        weekly.setTotalWorkingDays(counts.total());
        weekly.setWorkedDays(counts.worked());
        weekly.setAvailability(
                counts.total() == 0 ? 0 :
                        (counts.worked() * 100) / counts.total());

        attendanceWeeklyRepository.save(weekly);
        return weekly;
    }

    private WeeklySummaryResponse toWeeklyResponse(
            AttendanceWeekly weekly,
            int weekNumber,
            WeekRange range,
            WeeklyCounts counts
    ) {

        WeeklySummaryResponse w = new WeeklySummaryResponse();
        w.setWeekNumber(weekNumber);
        w.setStart(range.start());
        w.setEnd(range.end());
        w.setTotalWorkingDays(counts.total());
        w.setWorkedDays(counts.worked());
        w.setAvailability(weekly.getAvailability());
        return w;
    }

    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    // =====================================================
    // HELPER RECORDS (Java 21)
    // =====================================================
    private record WeekRange(LocalDate start, LocalDate end) {}
    private record WeeklyCounts(int total, int worked) {}
}

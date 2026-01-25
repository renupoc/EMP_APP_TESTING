package com.example.attendance.repository;

import com.example.attendance.entity.AttendanceWeekly;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface AttendanceWeeklyRepository
        extends JpaRepository<AttendanceWeekly, Long> {

    Optional<AttendanceWeekly>
    findByAttendance_IdAndYearAndMonthAndWeekNumber(
            Long attendanceId, int year, int month, int weekNumber
    );

    List<AttendanceWeekly>
    findByAttendance_IdAndYearAndMonth(
            Long attendanceId, int year, int month
    );
}
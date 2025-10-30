package com.studentmanagement.repository;  
  
import org.springframework.data.jpa.repository.JpaRepository;  
import com.studentmanagement.model.AttendanceRecord;  
import java.time.LocalDate;  
import java.util.List;  
  
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findByEnrollmentCourseIdAndDate(Long courseId, LocalDate date);
    List<AttendanceRecord> findByEnrollmentStudentId(Long studentId);
    List<AttendanceRecord> findByEnrollmentCourseId(Long courseId);
    List<AttendanceRecord> findByEnrollmentStudentIdAndEnrollmentCourseId(Long studentId, Long courseId);
}

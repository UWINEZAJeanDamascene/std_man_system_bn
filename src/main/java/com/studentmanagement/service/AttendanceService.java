package com.studentmanagement.service;  
  
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studentmanagement.model.AttendanceRecord;
import com.studentmanagement.repository.AttendanceRecordRepository;
import com.studentmanagement.repository.CourseRepository;
import com.studentmanagement.repository.StudentRepository;  

  
@Service  
public class AttendanceService {  
  
    @Autowired  
    private AttendanceRecordRepository attendanceRepository;  
  
    @Autowired  
    private CourseRepository courseRepository;  
  
    @Autowired  
    private StudentRepository studentRepository;  
  
    public List<AttendanceRecord> getAttendanceByCourseAndDate(Long courseId, LocalDate date) {  
        return attendanceRepository.findByEnrollmentCourseIdAndDate(courseId, date);  
    }  
  
    public AttendanceRecord saveAttendance(AttendanceRecord attendance) {  
        return attendanceRepository.save(attendance);  
    }  
  
    public List<AttendanceRecord> saveBulkAttendance(List<AttendanceRecord> attendances) {  
        return attendanceRepository.saveAll(attendances);  
    }  
  
    public List<AttendanceRecord> getAttendanceByStudent(Long studentId) {  
        return attendanceRepository.findByEnrollmentStudentId(studentId);  
    }  
  
    public List<AttendanceRecord> getAttendanceByCourse(Long courseId) {  
        return attendanceRepository.findByEnrollmentCourseId(courseId);  
    }

    public java.util.Optional<AttendanceRecord> findById(Long id) {
        return attendanceRepository.findById(id);
    }
  
    public double getAttendancePercentage(Long studentId, Long courseId) {  
        List<AttendanceRecord> records = attendanceRepository.findByEnrollmentStudentIdAndEnrollmentCourseId(studentId, courseId);
  
        if (records.isEmpty()) return 0.0;  
  
        long presentCount = records.stream()  
                .filter(record -> "Present".equals(record.getStatus()))  
                .count();  
  
        return (double) presentCount / records.size() * 100;  
    }

    @Transactional
    public boolean deleteAttendance(Long id) {
        try {
            // First try to find the record
            var record = attendanceRepository.findById(id);
            if (record.isEmpty()) {
                return false;
            }
            
            // Delete the record directly
            attendanceRepository.delete(record.get());
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting attendance record: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
} 

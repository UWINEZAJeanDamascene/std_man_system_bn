package com.studentmanagement.controller;  
  
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
  
import com.studentmanagement.model.AttendanceRecord;
import com.studentmanagement.model.Course;
import com.studentmanagement.model.Student;
import com.studentmanagement.service.AttendanceService;
import com.studentmanagement.service.CourseService;
import com.studentmanagement.service.StudentService;
  
@RestController  
@RequestMapping("/api/reports")  
@CrossOrigin  
public class ReportController {  
  
    @Autowired  
    private AttendanceService attendanceService;  
  
    @Autowired  
    private StudentService studentService;  
  
    @Autowired  
    private CourseService courseService;  
  
    @GetMapping("/student/{studentId}")  
    public Map<String, Object> getStudentReport(@PathVariable Long studentId) {  
        Student student = studentService.getStudentById(studentId).orElse(null);  
        if (student == null) return null;  
  
        List<AttendanceRecord> records = attendanceService.getAttendanceByStudent(studentId);  
        Map<String, Object> report = new HashMap<>();  
        report.put("student", student);  
        report.put("attendanceRecords", records);  
  
        // Calculate overall attendance percentage  
        long totalSessions = records.size();  
        long presentSessions = records.stream().filter(r -> "Present".equals(r.getStatus())).count();  
        double percentage;
        if (totalSessions > 0) {
            percentage = ((double) presentSessions / (double) totalSessions) * 100.0;
        } else {
            percentage = 0.0;
        }
        report.put("overallAttendancePercentage", percentage);  
  
        return report;  
    }  
  
    @GetMapping("/course/{courseId}")  
    public Map<String, Object> getCourseReport(@PathVariable Long courseId) {  
        Course course = courseService.getCourseById(courseId).orElse(null);  
        if (course == null) return null;  
  
        List<AttendanceRecord> records = attendanceService.getAttendanceByCourse(courseId);  
    List<Student> students = course.getEnrollments().stream().map(e -> e.getStudent()).collect(Collectors.toList());
  
        Map<String, Object> report = new HashMap<>();  
        report.put("course", course);  
        report.put("attendanceRecords", records);  
  
        // Calculate attendance for each student in the course  
        List<Map<String, Object>> studentReports = students.stream().map(student -> {  
            Map<String, Object> studentReport = new HashMap<>();  
            studentReport.put("student", student);  
            double percentage = attendanceService.getAttendancePercentage(student.getId(), courseId);  
            studentReport.put("attendancePercentage", percentage);  
            return studentReport;  
        }).collect(Collectors.toList());  
  
        report.put("studentReports", studentReports);  
        return report;  
    }  
} 

package com.studentmanagement.controller;  
  
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.PathVariable;  
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.studentmanagement.model.AttendanceRecord;
import com.studentmanagement.service.AttendanceService;
  
@RestController  
@RequestMapping("/api/attendance")  
@CrossOrigin(origins = "http://localhost:3000", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class AttendanceController {  
  
    @Autowired  
    private AttendanceService attendanceService;  
    
    @Autowired
    private com.studentmanagement.repository.EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private com.studentmanagement.repository.StudentRepository studentRepository;
  
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public List<AttendanceRecord> getAttendanceByCourse(@PathVariable Long courseId,  
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {  
        return attendanceService.getAttendanceByCourseAndDate(courseId, date);  
    }  
  
    @PostMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<List<AttendanceRecord>> markAttendance(@PathVariable Long courseId,  
            @RequestBody List<Map<String, Object>> attendanceData) {  
        List<AttendanceRecord> records = attendanceData.stream().map(data -> {
            Long studentId = ((Number) data.get("studentId")).longValue();
            // Protect against duplicate enrollments in DB by searching enrollments list
            java.util.List<com.studentmanagement.model.Enrollment> studentEnrollments = enrollmentRepository.findByStudentId(studentId);
            java.util.Optional<com.studentmanagement.model.Enrollment> enrollmentOpt = studentEnrollments.stream()
                    .filter(e -> e.getCourse() != null && e.getCourse().getId() != null && e.getCourse().getId().equals(courseId))
                    .findFirst();
            if (enrollmentOpt.isEmpty()) {
                // Return a clear 400 error when a student in the payload is not enrolled
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student is not enrolled in course: " + studentId);
            }
            AttendanceRecord record = new AttendanceRecord();
            record.setEnrollment(enrollmentOpt.get());
            record.setDate(LocalDate.now());
            record.setStatus((String) data.get("status"));
            return record;
        }).collect(Collectors.toList());
  
        List<AttendanceRecord> savedRecords = attendanceService.saveBulkAttendance(records);  
        return ResponseEntity.ok(savedRecords);  
    }  
  
    @GetMapping("/student/{studentId}")  
    public ResponseEntity<List<AttendanceRecord>> getAttendanceByStudent(@PathVariable Long studentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isLecturerOrAdmin = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_LECTURER") || a.getAuthority().equals("ROLE_ADMIN"));
        
        // If student, only allow access to own attendance records
        if (!isLecturerOrAdmin) {
            // auth.getName() is the username/email (JWT subject). Resolve student profile by email.
            var currentStudentOpt = studentRepository.findByEmail(auth.getName());
            if (currentStudentOpt.isEmpty()) {
                return ResponseEntity.status(403).build();
            }
            long currentUserId = currentStudentOpt.get().getId();
            if (!studentId.equals(currentUserId)) {
                return ResponseEntity.status(403).build();
            }
        }
        
        return ResponseEntity.ok(attendanceService.getAttendanceByStudent(studentId));  
    }  
  
    @GetMapping("/course/{courseId}/all")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public List<AttendanceRecord> getAllAttendanceByCourse(@PathVariable Long courseId) {  
        return attendanceService.getAttendanceByCourse(courseId);  
    }  
  
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<AttendanceRecord> updateAttendance(@PathVariable Long id, @RequestBody AttendanceRecord attendanceDetails) {  
        return attendanceService.getAttendanceByStudent(id).stream().findFirst()  
                .map(record -> {  
                    record.setStatus(attendanceDetails.getStatus());  
                    return ResponseEntity.ok(attendanceService.saveAttendance(record));  
                })  
                .orElse(ResponseEntity.notFound().build());  
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN', 'STUDENT')")
    public ResponseEntity<?> deleteAttendance(@PathVariable Long id) {
        try {
            // Get the attendance record by ID
            var attendanceRecord = attendanceService.findById(id);
            if (attendanceRecord.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Attendance record not found with id: " + id);
            }

            // Verify permissions
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isLecturerOrAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_LECTURER") || a.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isLecturerOrAdmin) {
                // For students, verify it's their own record
                var currentStudentOpt = studentRepository.findByEmail(auth.getName());
                if (currentStudentOpt.isEmpty() || 
                    !attendanceRecord.get().getEnrollment().getStudent().getId().equals(currentStudentOpt.get().getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You don't have permission to delete this record");
                }
            }

            try {
                boolean deleted = attendanceService.deleteAttendance(id);
                if (deleted) {
                    return ResponseEntity.ok().build();
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Unable to delete attendance record: " + id);
                }
            } catch (Exception e) {
                System.err.println("Error deleting attendance record: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error deleting attendance record", "message", e.getMessage()));
            }
        } catch (Exception e) {
            System.err.println("Error in delete endpoint: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Unexpected error", "message", e.getMessage()));
        }
    }
} 

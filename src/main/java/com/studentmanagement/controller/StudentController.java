package com.studentmanagement.controller;  
  
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;  
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;  
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.studentmanagement.model.Student;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.service.StudentService;
  
@RestController  
@RequestMapping("/api/students")  
@CrossOrigin  
public class StudentController {  
  
    @Autowired  
    private StudentService studentService;  

    @Autowired
    private StudentRepository studentRepository;
  
    @GetMapping
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public List<Student> getAllStudents() {  
        return studentService.getAllStudents();  
    }  
  
    @GetMapping("/{id}")  
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isLecturerOrAdmin = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_LECTURER") || a.getAuthority().equals("ROLE_ADMIN"));
        
        // If student, only allow access to own profile
        if (!isLecturerOrAdmin) {
            // auth.getName() contains the username/email (JWT subject), not the numeric user id.
            var currentStudentOpt = studentRepository.findByEmail(auth.getName());
            if (currentStudentOpt.isEmpty()) {
                return ResponseEntity.status(403).build();
            }
            long currentUserId = currentStudentOpt.get().getId();
            if (!id.equals(currentUserId)) {
                return ResponseEntity.status(403).build();
            }
        }
        
        return studentService.getStudentById(id)  
                .map(student -> ResponseEntity.ok(student))  
                .orElse(ResponseEntity.notFound().build());  
    }  
  
    @PostMapping
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public Student createStudent(@RequestBody Student student) {  
        return studentService.saveStudent(student);  
    }  
  
    @PutMapping("/{id}")  
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @RequestBody Student studentDetails) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isLecturerOrAdmin = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_LECTURER") || a.getAuthority().equals("ROLE_ADMIN"));
        
        // If student, only allow updating own profile
        if (!isLecturerOrAdmin) {
            long currentUserId = Long.parseLong(auth.getName());
            if (!id.equals(currentUserId)) {
                return ResponseEntity.status(403).build();
            }
        }
        
        return studentService.getStudentById(id)  
                .map(student -> {  
                    // If student role, only allow updating certain fields
                    if (!isLecturerOrAdmin) {
                        student.setPhone(studentDetails.getPhone());
                        // Add other safe-to-update fields here
                    } else {
                        student.setName(studentDetails.getName());  
                        student.setEmail(studentDetails.getEmail());  
                        student.setStudentClass(studentDetails.getStudentClass());  
                        student.setPhone(studentDetails.getPhone());
                    }
                    return ResponseEntity.ok(studentService.saveStudent(student));  
                })  
                .orElse(ResponseEntity.notFound().build());  
    }  
  
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {  
        if (studentService.getStudentById(id).isPresent()) {  
            studentService.deleteStudent(id);  
            return ResponseEntity.noContent().build();  
        }  
        return ResponseEntity.notFound().build();  
    }  
  
    @GetMapping("/class/{studentClass}")
    public List<Student> getStudentsByClass(@PathVariable String studentClass) {
        return studentService.getStudentsByClass(studentClass);
    }

    @GetMapping("/me")
    public ResponseEntity<Student> getCurrentStudent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var currentStudentOpt = studentRepository.findByEmail(auth.getName());
        if (currentStudentOpt.isEmpty()) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(currentStudentOpt.get());
    }
}

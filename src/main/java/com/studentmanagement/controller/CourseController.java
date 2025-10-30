package com.studentmanagement.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.studentmanagement.model.Course;
import com.studentmanagement.service.CourseService;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin
public class CourseController {
  
    @Autowired  
    private CourseService courseService;  
  
    @GetMapping({"/", "", "/all"})
    // Allow unauthenticated GET requests to list courses. Security configured in SecurityConfig
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }
  
    @GetMapping("/{id}")
    // Allow unauthenticated GET requests for course details (SecurityConfig allows GET)
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        return courseService.getCourseById(id)
                .map(course -> ResponseEntity.ok(course))
                .orElse(ResponseEntity.notFound().build());
    }
  
    @PostMapping
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<?> createCourse(@RequestBody Course course) {
        try {
            // Require that a lecturer object is provided (either existing id or new lecturer details)
            if (course.getLecturer() == null) {
                return ResponseEntity.badRequest().body("Lecturer is required");
            }
            Course savedCourse = courseService.saveCourse(course);
            return ResponseEntity.ok(savedCourse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating course: " + e.getMessage());
        }
    }
  
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @RequestBody Course courseDetails) {
        return courseService.getCourseById(id)
                .map(course -> {
                    course.setCourseName(courseDetails.getCourseName());
                    course.setLecturer(courseDetails.getLecturer());
                    return ResponseEntity.ok(courseService.saveCourse(course));
                })
                .orElse(ResponseEntity.notFound().build());
    }
  
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        if (courseService.getCourseById(id).isPresent()) {
            courseService.deleteCourse(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
  
    @GetMapping("/lecturer/{lecturerId}")  
    public List<Course> getCoursesByLecturer(@PathVariable Long lecturerId) {  
        return courseService.getCoursesByLecturerId(lecturerId);  
    }  
  
    @PostMapping("/{courseId}/enroll/{studentId}")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<Course> enrollStudent(@PathVariable Long courseId, @PathVariable Long studentId) {
        try {
            Course course = courseService.enrollStudent(courseId, studentId);
            return ResponseEntity.ok(course);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
  
    @DeleteMapping("/{courseId}/unenroll/{studentId}")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<Course> unenrollStudent(@PathVariable Long courseId, @PathVariable Long studentId) {
        try {
            Course course = courseService.unenrollStudent(courseId, studentId);
            return ResponseEntity.ok(course);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{courseId}/enroll-self")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> enrollSelf(@PathVariable Long courseId) {
        try {
            Course course = courseService.enrollSelf(courseId);
            return ResponseEntity.ok(course);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{courseId}/unenroll-self")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> unenrollSelf(@PathVariable Long courseId) {
        try {
            Course course = courseService.unenrollSelf(courseId);
            return ResponseEntity.ok(course);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 

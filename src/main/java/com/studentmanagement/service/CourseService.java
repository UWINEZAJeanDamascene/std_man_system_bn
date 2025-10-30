package com.studentmanagement.service;  
  
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.studentmanagement.model.Course;
import com.studentmanagement.model.Student;
import com.studentmanagement.model.User;
import com.studentmanagement.repository.CourseRepository;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.repository.UserRepository;  
  
@Service  
public class CourseService {  
  
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.studentmanagement.repository.EnrollmentRepository enrollmentRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
  
    public List<Course> getAllCourses() {  
        return courseRepository.findAll();  
    }  
  
    public Optional<Course> getCourseById(Long id) {  
        return courseRepository.findById(id);  
    }  
  
    public Course saveCourse(Course course) {
        // Handle lecturer: accept existing lecturer by id, or create a new lecturer if details provided
        if (course.getLecturer() != null) {
            User lec = course.getLecturer();
            if (lec.getId() != null) {
                User existing = userRepository.findById(lec.getId()).orElseThrow(() -> new RuntimeException("Lecturer not found"));
                course.setLecturer(existing);
            } else {
                // Create new lecturer user. Ensure role and password are set and encoded.
                if (lec.getRole() == null || lec.getRole().isEmpty()) {
                    lec.setRole("ROLE_LECTURER");
                } else if (!lec.getRole().startsWith("ROLE_")) {
                    lec.setRole("ROLE_" + lec.getRole());
                }
                if (lec.getPassword() == null || lec.getPassword().isEmpty()) {
                    // set a temporary password; caller should change it later
                    lec.setPassword(passwordEncoder.encode("change_me"));
                } else {
                    lec.setPassword(passwordEncoder.encode(lec.getPassword()));
                }
                User saved = userRepository.save(lec);
                course.setLecturer(saved);
            }
        } else {
            throw new RuntimeException("Lecturer is required for a course");
        }

        Course saved = courseRepository.save(course);

        // Handle students provided transiently on Course: create Enrollment rows
        if (course.getStudents() != null && !course.getStudents().isEmpty()) {
            for (Student s : course.getStudents()) {
                Student existing = null;
                if (s.getId() != null) {
                    existing = studentRepository.findById(s.getId()).orElseThrow(() -> new RuntimeException("Student not found"));
                } else if (s.getEmail() != null && !s.getEmail().isEmpty()) {
                    existing = studentRepository.findByEmail(s.getEmail()).orElse(null);
                    if (existing == null) {
                        Student toSave = new Student();
                        toSave.setName(s.getName());
                        toSave.setEmail(s.getEmail());
                        toSave.setPhone(s.getPhone());
                        toSave.setStudentClass(s.getStudentClass());
                        existing = studentRepository.save(toSave);
                    }
                } else {
                    throw new RuntimeException("Student must have id or email");
                }

                // create enrollment if not exists
                java.util.Optional<com.studentmanagement.model.Enrollment> existingEnrollment = enrollmentRepository.findByStudentIdAndCourseId(existing.getId(), saved.getId());
                if (existingEnrollment.isEmpty()) {
                    com.studentmanagement.model.Enrollment e = new com.studentmanagement.model.Enrollment();
                    e.setCourse(saved);
                    e.setStudent(existing);
                    enrollmentRepository.save(e);
                }
            }
        }

        return saved;
    }
  
    public void deleteCourse(Long id) {  
        courseRepository.deleteById(id);  
    }  
  
    public List<Course> getCoursesByLecturerId(Long lecturerId) {
        return courseRepository.findByLecturerId(lecturerId);
    }

    public User findLecturerById(Long lecturerId) {
        return userRepository.findById(lecturerId).orElse(null);
    }

    public Student getStudentByEmail(String email) {
        return studentRepository.findByEmail(email).orElse(null);
    }

    public Course assignLecturerToCourse(Long courseId, Long lecturerId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));
        User lecturer = userRepository.findById(lecturerId).orElseThrow(() -> new RuntimeException("Lecturer not found"));
        course.setLecturer(lecturer);
        return courseRepository.save(course);
    }
  
    public Course enrollStudent(Long courseId, Long studentId) {  
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));
        Student student = studentRepository.findById(studentId).orElseThrow(() -> new RuntimeException("Student not found"));
        // create enrollment if not exists
        java.util.Optional<com.studentmanagement.model.Enrollment> existing = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId);
        if (existing.isEmpty()) {
            com.studentmanagement.model.Enrollment e = new com.studentmanagement.model.Enrollment();
            e.setCourse(course);
            e.setStudent(student);
            enrollmentRepository.save(e);
        }
        return course;
    }  
  
    public Course unenrollStudent(Long courseId, Long studentId) {  
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));
        Student student = studentRepository.findById(studentId).orElseThrow(() -> new RuntimeException("Student not found"));
        java.util.Optional<com.studentmanagement.model.Enrollment> existing = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId);
        if (existing.isPresent()) {
            enrollmentRepository.delete(existing.get());
        }
        return course;
    }

    public Course enrollSelf(Long courseId) {
        // Get current user's email from security context
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Student not found for current user"));
        return enrollStudent(courseId, student.getId());
    }

    public Course unenrollSelf(Long courseId) {
        // Get current user's email from security context
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Student not found for current user"));
        return unenrollStudent(courseId, student.getId());
    }
} 

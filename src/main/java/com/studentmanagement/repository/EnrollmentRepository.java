package com.studentmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studentmanagement.model.Enrollment;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.course.id = :courseId ORDER BY e.enrolledAt DESC")
    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);
    List<Enrollment> findByCourseId(Long courseId);
    List<Enrollment> findByStudentId(Long studentId);
}

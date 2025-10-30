package com.studentmanagement.model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String courseName;

    @ManyToOne(optional = false)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private User lecturer;

    // Persisted enrollments for this course. Ignore when serializing to avoid cycles.
    @JsonIgnore
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Enrollment> enrollments;

    // Transient compatibility field: accept students list in incoming DTOs (not persisted)
    @Transient
    private Set<Student> students;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public User getLecturer() { return lecturer; }
    public void setLecturer(User lecturer) { this.lecturer = lecturer; }

    public Set<Student> getStudents() {
        if (this.students != null) return this.students;
        if (this.enrollments == null) return java.util.Collections.emptySet();
        java.util.Set<Student> s = new java.util.HashSet<>();
        for (Enrollment e : this.enrollments) {
            if (e != null && e.getStudent() != null) s.add(e.getStudent());
        }
        return s;
    }
    public void setStudents(Set<Student> students) { this.students = students; }

    public Set<Enrollment> getEnrollments() { return enrollments; }
    public void setEnrollments(Set<Enrollment> enrollments) { this.enrollments = enrollments; }

}

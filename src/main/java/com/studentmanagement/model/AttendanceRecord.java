package com.studentmanagement.model;  
  
import jakarta.persistence.*;  
import java.time.LocalDate;  
  
@Entity  
@Table(name = "attendance_records")  
public class AttendanceRecord {  
  
    @Id  
    @GeneratedValue(strategy = GenerationType.IDENTITY)  
    private Long id;  
  
    @ManyToOne
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    private LocalDate date;
    private String status; // Present, Absent, Late

    @ManyToOne
    @JoinColumn(name = "lecturer_id")
    private User lecturer;
  
    // Getters and Setters  
    public Long getId() { return id; }  
    public void setId(Long id) { this.id = id; }  
  
    public Enrollment getEnrollment() { return enrollment; }
    public void setEnrollment(Enrollment enrollment) { this.enrollment = enrollment; }

    public LocalDate getDate() { return date; }  
    public void setDate(LocalDate date) { this.date = date; }  
  
    public String getStatus() { return status; }  
    public void setStatus(String status) { this.status = status; }  
  
    public User getLecturer() { return lecturer; }
    public void setLecturer(User lecturer) { this.lecturer = lecturer; }  
} 

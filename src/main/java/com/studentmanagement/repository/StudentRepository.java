package com.studentmanagement.repository;  
  
import org.springframework.data.jpa.repository.JpaRepository;  
import com.studentmanagement.model.Student;  
import java.util.List;  
  
public interface StudentRepository extends JpaRepository<Student, Long> {  
    List<Student> findByStudentClass(String studentClass);  
    java.util.Optional<Student> findByEmail(String email);
} 

package com.studentmanagement.repository;  
  
import org.springframework.data.jpa.repository.JpaRepository;  
import com.studentmanagement.model.Course;  
import java.util.List;  
  
public interface CourseRepository extends JpaRepository<Course, Long> {  
    List<Course> findByLecturerId(Long lecturerId);  
} 

package com.studentmanagement.service;  
  
import com.studentmanagement.model.Student;  
import com.studentmanagement.repository.StudentRepository;  
import org.springframework.beans.factory.annotation.Autowired;  
import org.springframework.stereotype.Service;  
import java.util.List;  
import java.util.Optional;  
  
@Service  
public class StudentService {  
  
    @Autowired  
    private StudentRepository studentRepository;  
  
    public List<Student> getAllStudents() {  
        return studentRepository.findAll();  
    }  
  
    public Optional<Student> getStudentById(Long id) {  
        return studentRepository.findById(id);  
    }  
  
    public Student saveStudent(Student student) {  
        return studentRepository.save(student);  
    }  
  
    public void deleteStudent(Long id) {  
        studentRepository.deleteById(id);  
    }  
  
    public List<Student> getStudentsByClass(String studentClass) {  
        return studentRepository.findByStudentClass(studentClass);  
    }  
} 

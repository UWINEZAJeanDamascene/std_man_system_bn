package com.studentmanagement.repository;  
  
import org.springframework.data.jpa.repository.JpaRepository;  
import com.studentmanagement.model.User;  
import java.util.Optional;  
  
public interface UserRepository extends JpaRepository<User, Long> {  
    Optional<User> findByEmail(String email);  
    java.util.List<User> findByRole(String role);
} 

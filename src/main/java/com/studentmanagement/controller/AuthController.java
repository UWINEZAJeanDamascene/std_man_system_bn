package com.studentmanagement.controller;  
  
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;  
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.studentmanagement.model.Student;
import com.studentmanagement.model.User;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.repository.UserRepository;
import com.studentmanagement.security.JwtUtil;
  
@RestController  
@RequestMapping("/api/auth")  
@CrossOrigin  
public class AuthController {  
  
    @Autowired  
    private AuthenticationManager authenticationManager;  
  
    @Autowired  
    private JwtUtil jwtUtil;  
  
    @Autowired  
    private UserRepository userRepository;  
  
    @Autowired  
    private PasswordEncoder passwordEncoder;  

    @Autowired
    private StudentRepository studentRepository;

    // No invite code required: allow public registration as STUDENT or LECTURER
  
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));

            User user = userRepository.findByEmail(email).orElseThrow(() -> 
                new UsernameNotFoundException("User not found with email: " + email));
            
            // Ensure role has ROLE_ prefix
            String role = user.getRole();
            if (!role.startsWith("ROLE_")) {
                role = "ROLE_" + role;
                user.setRole(role);
                userRepository.save(user);
            }
            
            String token = jwtUtil.generateToken(email, role);
            
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("role", user.getRole());
            response.put("userId", user.getId().toString());
            response.put("name", user.getName());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid email or password"));
        }
    }
  
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, Object> payload) {
        // Extract fields from payload
        String name = payload.getOrDefault("name", "").toString();
        String email = payload.getOrDefault("email", "").toString();
        String password = payload.getOrDefault("password", "").toString();
        String clientRole = payload.getOrDefault("role", "STUDENT").toString();
        String inviteCode = payload.getOrDefault("inviteCode", "").toString();

        if (email.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and password are required"));
        }

        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
        }

        // Normalize role
        String roleKey = clientRole.toUpperCase().startsWith("ROLE_") ? clientRole.toUpperCase().substring(5) : clientRole.toUpperCase();
        String normalizedRole;
        // Debug log incoming role
        System.out.println("Register payload role: '" + clientRole + "', normalized key: '" + roleKey + "'");
        if ("STUDENT".equals(roleKey)) {
            normalizedRole = "ROLE_STUDENT";
        } else if ("LECTURER".equals(roleKey)) {
            // Allow creating lecturers publicly (system will save as ROLE_LECTURER)
            normalizedRole = "ROLE_LECTURER";
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role. Allowed: STUDENT, LECTURER"));
        }

        // Create and save user
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(normalizedRole);

        User saved = userRepository.save(user);

        // If student, create Student profile
        if ("ROLE_STUDENT".equals(normalizedRole)) {
            if (studentRepository.findByEmail(saved.getEmail()).isEmpty()) {
                Student student = new Student();
                student.setName(saved.getName());
                student.setEmail(saved.getEmail());
                studentRepository.save(student);
            }
        }

        return ResponseEntity.ok(Map.of("message", "User registered successfully", "role", saved.getRole()));
    }
} 

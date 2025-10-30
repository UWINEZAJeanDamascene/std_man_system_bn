package com.studentmanagement.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.studentmanagement.model.User;
import com.studentmanagement.repository.UserRepository;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Return users with exact role (e.g., ROLE_LECTURER or LECTURER depending on storage)
    @GetMapping("/lecturers")
    public List<User> getLecturers() {
        // Try both ROLE_LECTURER and LECTURER
        List<User> res = userRepository.findByRole("ROLE_LECTURER");
        if (res == null || res.isEmpty()) {
            res = userRepository.findByRole("LECTURER");
        }
        return res;
    }

    // Create a lecturer user (password will be encoded). Restricted to users with ROLE_LECTURER or ROLE_ADMIN.
    @PostMapping("/create-lecturer")
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN')")
    public ResponseEntity<?> createLecturer(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("ROLE_LECTURER");
        } else if (!user.getRole().startsWith("ROLE_")) {
            user.setRole("ROLE_" + user.getRole());
        }
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }
}

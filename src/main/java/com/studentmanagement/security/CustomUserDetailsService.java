package com.studentmanagement.security;  
  
import com.studentmanagement.model.User;  
import com.studentmanagement.repository.UserRepository;  
import org.springframework.beans.factory.annotation.Autowired;  
import org.springframework.security.core.userdetails.UserDetails;  
import org.springframework.security.core.userdetails.UserDetailsService;  
import org.springframework.security.core.userdetails.UsernameNotFoundException;  
import org.springframework.stereotype.Service;  
import java.util.ArrayList;  
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
  
@Service  
public class CustomUserDetailsService implements UserDetailsService {  
  
    @Autowired  
    private UserRepository userRepository;  
  
    @Override  
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {  
        User user = userRepository.findByEmail(email)  
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));  
        // Map stored role (e.g., "LECTURER") to GrantedAuthority format expected by Spring Security (ROLE_...)
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (user.getRole() != null && !user.getRole().isEmpty()) {
            // Ensure role has ROLE_ prefix when creating authorities
            String roleName = user.getRole();
            if (!roleName.startsWith("ROLE_")) {
                roleName = "ROLE_" + roleName;
            }
            authorities.add(new SimpleGrantedAuthority(roleName));
        }
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);  
    }  
} 

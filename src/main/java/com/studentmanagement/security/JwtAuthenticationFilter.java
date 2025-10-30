package com.studentmanagement.security;  
  
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;  
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;  
  
@Component  
public class JwtAuthenticationFilter extends OncePerRequestFilter {  
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired  
    private JwtUtil jwtUtil;  
  
    @Autowired  
    private UserDetailsService userDetailsService;  
  
    @Override  
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)  
            throws ServletException, IOException {  
  
        final String authorizationHeader = request.getHeader("Authorization");  
        
        logger.debug("Processing request: {} {}", request.getMethod(), request.getRequestURI());
        String username = null;  
        String jwt = null;  
  
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {  
            jwt = authorizationHeader.substring(7);  
            try {
                username = jwtUtil.extractUsername(jwt);
                logger.debug("Extracted username from JWT: {}", username);
            } catch (Exception e) {
                logger.error("Failed to extract username from JWT", e);
            }
        } else {
            logger.debug("No JWT token found in request headers");
        }
  
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {  
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                logger.debug("Loaded user details for {}: roles = {}", username, 
                    userDetails.getAuthorities().stream()
                        .map(auth -> auth.getAuthority())
                        .collect(java.util.stream.Collectors.joining(", ")));
                
                if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(  
                            userDetails, null, userDetails.getAuthorities());  
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));  
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("Successfully authenticated user {}", username);
                } else {
                    logger.warn("JWT token validation failed for user {}", username);
                }
            } catch (Exception e) {
                logger.error("Authentication failed for user {}", username, e);
            }
        }

        chain.doFilter(request, response);  
    }  
} 

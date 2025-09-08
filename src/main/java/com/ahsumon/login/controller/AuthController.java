package com.ahsumon.login.controller;

import com.ahsumon.login.dto.RegisterRequest;
import com.ahsumon.login.entity.UserDetailsEntity;
import com.ahsumon.login.entity.UserEntity;
import com.ahsumon.login.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder;
    private final AuthenticationManager authManager;

    private final UserDetailsService userDetailsService; // Inject the UserDetailsService

    // Constructor updated to include UserDetailsService
    public AuthController(UserRepository repo, BCryptPasswordEncoder encoder, AuthenticationManager authManager,  UserDetailsService userDetailsService) {
        this.repo = repo;
        this.encoder = encoder;
        this.authManager = authManager;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        if (repo.findByUsername(request.getUsername()).isPresent()) {
            return "User already exists!";
        }

        // Create UserEntity
        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        // Create UserDetailsEntity
        UserDetailsEntity details = new UserDetailsEntity();
        details.setFullName(request.getFullName());
        details.setEmail(request.getEmail());
        details.setContactNumber(request.getContactNumber());
        details.setUser(user);

        // Link both
        user.setUserDetails(details);

        repo.save(user);
        return "User registered successfully!";
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(body.get("username"), body.get("password"))
            );
        } catch (Exception e) {
            // Throw a 401 Unauthorized status if authentication fails
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password", e);
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(body.get("username"));


        return ResponseEntity.ok(userDetails);
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello, this is a secured endpoint!";
    }


    @PreAuthorize("hasRole('USER')")
    @PutMapping("/update/{username}")
    public ResponseEntity<String> updateUser(
            @PathVariable String username,
            @RequestBody RegisterRequest request
    ) {
        UserEntity user = repo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Update fields
        user.setPassword(encoder.encode(request.getPassword())); // you may not want to update username
        user.setRole(request.getRole());

        UserDetailsEntity details = user.getUserDetails();
        details.setFullName(request.getFullName());
        details.setEmail(request.getEmail());
        details.setContactNumber(request.getContactNumber());

        user.setUserDetails(details);

        repo.save(user);
        return ResponseEntity.ok("User updated successfully!");
    }

}

package com.ahsumon.login.service;



import com.ahsumon.login.dto.RegisterRequest;
import com.ahsumon.login.entity.UserDetailsEntity;
import com.ahsumon.login.entity.UserEntity;
import com.ahsumon.login.repository.UserRepository;
import com.ahsumon.login.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;

@Service
public class UserService {

    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository repo, BCryptPasswordEncoder encoder, AuthenticationManager authManager, JwtUtil jwtUtil) {
        this.repo = repo;
        this.encoder = encoder;
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
    }
    public String registerUser(RegisterRequest request) {
        if (repo.findByUsername(request.getUsername()).isPresent()) {
            return "User already exists!";
        }

        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        UserDetailsEntity details = new UserDetailsEntity();
        details.setFullName(request.getFullName());
        details.setEmail(request.getEmail());
        details.setContactNumber(request.getContactNumber());
        details.setUser(user);

        user.setUserDetails(details);
        repo.save(user);
        return "User registered successfully!";
    }

    public List<UserEntity> findAll() {
        return repo.findAll();
    }

    public void updateUser(String username, RegisterRequest request) {
        UserEntity user = repo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setPassword(encoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        UserDetailsEntity details = user.getUserDetails();
        if (details == null) {
            details = new UserDetailsEntity();
            details.setUser(user);
        }

        details.setFullName(request.getFullName());
        details.setEmail(request.getEmail());
        details.setContactNumber(request.getContactNumber());

        user.setUserDetails(details);

        repo.save(user);
    }

    public void deleteUser(String username) {
        UserEntity user = repo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        repo.delete(user);
    }

    public String authenticateUser(String username, String password) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        UserEntity user = repo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Generate JWT
        return jwtUtil.generateToken(user.getUsername(), user.getRole().name());
    }
    public Collection<GrantedAuthority> getAuthorities(String username) {
        UserEntity user = repo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }



}

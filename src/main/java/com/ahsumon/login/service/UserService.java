package com.ahsumon.login.service;

import com.ahsumon.login.dto.RegisterRequest;
import com.ahsumon.login.entity.UserDetailsEntity;
import com.ahsumon.login.entity.UserEntity;
import com.ahsumon.login.repository.UserRepository;
import com.ahsumon.login.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
        log.info("Attempting to register user: {}", request.getUsername());
        if (repo.findByUsername(request.getUsername()).isPresent()) {
            log.warn("Registration failed. User {} already exists", request.getUsername());
            return "User already exists!";
        }
        try {
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

            log.info("User {} registered successfully", request.getUsername());
            return "User registered successfully!";
        } catch (Exception e) {
            log.error("Error registering user {}", request.getUsername(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error registering user");
        }
    }

    public List<UserEntity> findAll() {
        log.debug("Fetching all users");
        return repo.findAll();
    }

    public void updateUser(String username, RegisterRequest request) {
        log.info("Updating user: {}", username);
        UserEntity user = repo.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User {} not found for update", username);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        try {
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
            log.info("User {} updated successfully", username);
        } catch (Exception e) {
            log.error("Error updating user {}", username, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating user");
        }
    }

    public void deleteUser(String username) {
        log.info("Deleting user: {}", username);
        UserEntity user = repo.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User {} not found for deletion", username);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });
        try {
            repo.delete(user);
            log.info("User {} deleted successfully", username);
        } catch (Exception e) {
            log.error("Error deleting user {}", username, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting user");
        }
    }

    public String authenticateUser(String username, String password) {
        log.info("Authenticating user: {}", username);
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (Exception e) {
            log.warn("Authentication failed for user {}", username);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        UserEntity user = repo.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User {} not found after authentication", username);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        log.debug("JWT generated for user {}: {}", username, token);
        return token;
    }

    public Collection<GrantedAuthority> getAuthorities(String username) {
        log.debug("Getting authorities for user {}", username);
        UserEntity user = repo.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User {} not found while fetching authorities", username);
                    return new RuntimeException("User not found");
                });
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }
}

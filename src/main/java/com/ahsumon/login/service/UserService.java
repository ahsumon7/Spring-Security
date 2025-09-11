package com.ahsumon.login.service;



import com.ahsumon.login.dto.RegisterRequest;
import com.ahsumon.login.entity.UserDetailsEntity;
import com.ahsumon.login.entity.UserEntity;
import com.ahsumon.login.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder;

    public UserService(UserRepository repo, BCryptPasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
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
}

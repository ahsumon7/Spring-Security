package com.ahsumon.login.service;

import com.ahsumon.login.entity.UserEntity; // Your custom user entity class
import com.ahsumon.login.repository.UserRepository;
import org.springframework.security.core.userdetails.User; // Spring Security's User class
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository repo;

    public CustomUserDetailsService(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find the user by username. The repository returns an Optional.
        // We throw an exception if the user is not found.
        UserEntity userEntity = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Use the UserEntity object to build the Spring Security UserDetails object.
        return User.builder()
                .username(userEntity.getUsername())
                .password(userEntity.getPassword())
                .roles(userEntity.getRole().name())
                .build();
    }
}

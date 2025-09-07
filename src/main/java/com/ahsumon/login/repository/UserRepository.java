package com.ahsumon.login.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ahsumon.login.entity.UserEntity; // ✅ Import your entity
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> { // ✅ Use your entity
    Optional<UserEntity> findByUsername(String username);
}

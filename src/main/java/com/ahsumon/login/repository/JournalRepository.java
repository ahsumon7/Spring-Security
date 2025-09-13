package com.ahsumon.login.repository;

import com.ahsumon.login.entity.JournalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JournalRepository extends JpaRepository<JournalEntity, Long> {
    List<JournalEntity> findByOwnerUsername(String username);
}

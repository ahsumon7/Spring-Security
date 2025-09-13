package com.ahsumon.login.service;

import com.ahsumon.login.dto.JournalResponseDto;
import com.ahsumon.login.dto.UserDetailsResponseDto;
import com.ahsumon.login.dto.UserResponseDto;
import com.ahsumon.login.entity.JournalEntity;
import com.ahsumon.login.entity.UserEntity;
import com.ahsumon.login.entity.Role;
import com.ahsumon.login.repository.JournalRepository;
import com.ahsumon.login.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JournalService {

    private final JournalRepository journalRepository;
    private final UserRepository userRepository;

    public JournalService(JournalRepository journalRepository, UserRepository userRepository) {
        this.journalRepository = journalRepository;
        this.userRepository = userRepository;
    }

    private UserEntity getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElseThrow();
    }

    // Create journal (owner is always current user)
    public JournalEntity create(JournalEntity journal) {
        journal.setOwner(getCurrentUser());
        return journalRepository.save(journal);
    }

    // Get journals based on role
    public List<JournalResponseDto> getMyJournals() {
        UserEntity current = getCurrentUser();
        List<JournalEntity> journals;

        if (current.getRole() == Role.ADMIN) {
            journals = journalRepository.findAll();
        } else {
            journals = journalRepository.findByOwnerUsername(current.getUsername());
        }

        return journals.stream()
                .map(j -> new JournalResponseDto(
                        j.getId(),
                        j.getTitle(),
                        j.getContent(),
                        new UserResponseDto(
                                j.getOwner().getId(),
                                j.getOwner().getUsername(),
                                j.getOwner().getRole(),
                                new UserDetailsResponseDto(
                                        j.getOwner().getUserDetails().getFullName(),
                                        j.getOwner().getUserDetails().getEmail(),
                                        j.getOwner().getUserDetails().getContactNumber()
                                )
                        )
                ))
                .toList();
    }


    // Update journal
    public JournalEntity update(Long id, JournalEntity updated) {
        JournalEntity journal = journalRepository.findById(id).orElseThrow();
        UserEntity current = getCurrentUser();

        // Allow update if current user is owner OR admin
        if (!journal.getOwner().getUsername().equals(current.getUsername())
                && current.getRole() != Role.ADMIN) {
            throw new SecurityException("You cannot edit this journal");
        }

        journal.setTitle(updated.getTitle());
        journal.setContent(updated.getContent());
        return journalRepository.save(journal);
    }

    // Delete journal
    public void delete(Long id) {
        JournalEntity journal = journalRepository.findById(id).orElseThrow();
        UserEntity current = getCurrentUser();

        // Allow delete if current user is owner OR admin
        if (!journal.getOwner().getUsername().equals(current.getUsername())
                && current.getRole() != Role.ADMIN) {
            throw new SecurityException("You cannot delete this journal");
        }

        journalRepository.delete(journal);
    }



}

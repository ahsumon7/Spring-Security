package com.ahsumon.login.controller;

import com.ahsumon.login.dto.JournalResponseDto;
import com.ahsumon.login.entity.JournalEntity;
import com.ahsumon.login.service.JournalService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/journals")
public class JournalController {

    private final JournalService journalService;

    public JournalController(JournalService journalService) {
        this.journalService = journalService;
    }

    @PostMapping
    public JournalEntity create(@RequestBody JournalEntity journal) {
        return journalService.create(journal);
    }

    @GetMapping
    public List<JournalResponseDto> getMyJournals() {
        return journalService.getMyJournals();
    }

    @PutMapping("/{id}")
    public JournalEntity update(@PathVariable Long id, @RequestBody JournalEntity journal) {
        return journalService.update(id, journal);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        journalService.delete(id);
    }
}

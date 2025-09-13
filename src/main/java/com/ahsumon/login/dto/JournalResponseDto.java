package com.ahsumon.login.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalResponseDto {
    private Long id;
    private String title;
    private String content;
    private UserResponseDto owner; // expose owner info without password
}

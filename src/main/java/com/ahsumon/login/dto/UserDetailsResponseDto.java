package com.ahsumon.login.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsResponseDto {
    private String fullName;
    private String email;
    private String contactNumber;
}

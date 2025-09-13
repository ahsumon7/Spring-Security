package com.ahsumon.login.dto;

import com.ahsumon.login.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String username;
    private Role role;
    private UserDetailsResponseDto userDetails;
}

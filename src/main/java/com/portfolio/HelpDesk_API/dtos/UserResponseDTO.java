package com.portfolio.HelpDesk_API.dtos;

import com.portfolio.HelpDesk_API.entities.Role;
import com.portfolio.HelpDesk_API.entities.User;

public record UserResponseDTO(
        Long id,
        String name,
        String email,
        Role role
) {
    public UserResponseDTO(User user) {
        this(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}

package com.portfolio.HelpDesk_API.dtos;

import com.portfolio.HelpDesk_API.entities.User;
import jakarta.validation.constraints.Email;

public record UserResponseDTO(
        Long id,
        String name,
        @Email
        String email
) {
    public UserResponseDTO(User user) {
        this(user.getId(), user.getName(), user.getEmail());
    }
}

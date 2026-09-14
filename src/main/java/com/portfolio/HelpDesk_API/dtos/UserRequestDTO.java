package com.portfolio.HelpDesk_API.dtos;

import jakarta.validation.constraints.Email;

public record UserRequestDTO(
        String name,
        @Email
        String email
) {
}

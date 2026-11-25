package com.portfolio.HelpDesk_API.dtos;

import jakarta.validation.constraints.Email;

public record UserUpdateDTO(
        String name,
        @Email
        String email
) {
}
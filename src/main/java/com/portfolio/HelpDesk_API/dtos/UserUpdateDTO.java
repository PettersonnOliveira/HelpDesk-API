package com.portfolio.HelpDesk_API.dtos;

import jakarta.validation.constraints.Email;

public record UserUpdateDTO(
        Long id,
        String name,
        @Email
        String email
) {
    public UserUpdateDTO(Long id, String name, @Email
    String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
}

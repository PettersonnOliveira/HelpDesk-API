package com.portfolio.HelpDesk_API.dtos;

public record AuthResponseDTO(
        String token,
        String tokenType,
        UserResponseDTO user
) {
    public AuthResponseDTO(String token, UserResponseDTO user) {
        this(token, "Bearer", user);
    }
}


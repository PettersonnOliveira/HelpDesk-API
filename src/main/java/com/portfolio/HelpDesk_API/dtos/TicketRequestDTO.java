package com.portfolio.HelpDesk_API.dtos;

import jakarta.validation.constraints.NotBlank;

public record TicketRequestDTO(
        @NotBlank
        String title,
        String description
) {
}
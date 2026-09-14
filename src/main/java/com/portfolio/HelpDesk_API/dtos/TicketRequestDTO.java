package com.portfolio.HelpDesk_API.dtos;

import com.portfolio.HelpDesk_API.entities.Status;
import jakarta.validation.constraints.NotBlank;


public record TicketRequestDTO(
        @NotBlank
        String title,
        String description,
       Long userId
) {
}

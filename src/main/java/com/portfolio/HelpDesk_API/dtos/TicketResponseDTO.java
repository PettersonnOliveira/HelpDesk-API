package com.portfolio.HelpDesk_API.dtos;

import com.portfolio.HelpDesk_API.entities.Status;
import com.portfolio.HelpDesk_API.entities.Ticket;

import java.time.LocalDateTime;

public record TicketResponseDTO(
        Long id,
        String title,
        String description,
        Status status,
        LocalDateTime createdAt,
        UserResponseDTO user) {
    public TicketResponseDTO(Ticket ticket) {
        this(ticket.getId(), ticket.getTitle(), ticket.getDescription(), ticket.getStatus(), ticket.getCreatedAt(),
                ticket.getUser() != null ? new UserResponseDTO(ticket.getUser()) : null);
    };
}

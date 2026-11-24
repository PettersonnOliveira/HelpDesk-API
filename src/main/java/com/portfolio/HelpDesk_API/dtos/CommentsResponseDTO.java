package com.portfolio.HelpDesk_API.dtos;

import com.portfolio.HelpDesk_API.entities.Comments;

import java.time.LocalDateTime;

public record CommentsResponseDTO(
        Long id,
        String message,
        LocalDateTime createdAt,
       TicketResponseDTO ticket,
        UserResponseDTO user) {
    public CommentsResponseDTO(Comments comments){
        this(comments.getId(), comments.getMessage(), comments.getCreatedAt(),
                new TicketResponseDTO(comments.getTicket()),
                comments.getUser() != null ? new UserResponseDTO(comments.getUser()) : null);
    }
}

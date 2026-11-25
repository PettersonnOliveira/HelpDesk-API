package com.portfolio.HelpDesk_API.dtos;

public record CommentsRequestDTO(
        String message,
        Long ticketId
) {
}
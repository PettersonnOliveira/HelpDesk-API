package com.portfolio.HelpDesk_API.dtos;



import java.time.LocalDateTime;

public record CommentsRequestDTO(
        String message,
        Long ticketId
) {
}

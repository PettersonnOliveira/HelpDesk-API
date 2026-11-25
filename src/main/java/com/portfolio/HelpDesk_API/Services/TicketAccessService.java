package com.portfolio.HelpDesk_API.Services;

import com.portfolio.HelpDesk_API.Exceptions.ResourceNotFoundException;
import com.portfolio.HelpDesk_API.Repositories.TicketRepository;
import com.portfolio.HelpDesk_API.Security.SecurityUtils;
import com.portfolio.HelpDesk_API.entities.Ticket;
import org.springframework.stereotype.Service;

/**
 * Centraliza a busca de tickets com validação de acesso, evitando que a mesma
 * regra seja duplicada em TicketService e CommentService.
 */
@Service
public class TicketAccessService {

    private final TicketRepository ticketRepository;

    public TicketAccessService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public Ticket findTicketWithAccessCheck(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket não encontrado"));
        SecurityUtils.requireTicketAccess(ticket);
        return ticket;
    }
}

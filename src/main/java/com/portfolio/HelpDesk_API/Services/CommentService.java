package com.portfolio.HelpDesk_API.Services;

import com.portfolio.HelpDesk_API.Exceptions.BusinessRuleException;
import com.portfolio.HelpDesk_API.Repositories.CommentsRepository;
import com.portfolio.HelpDesk_API.Security.SecurityUtils;
import com.portfolio.HelpDesk_API.dtos.CommentsRequestDTO;
import com.portfolio.HelpDesk_API.dtos.CommentsResponseDTO;
import com.portfolio.HelpDesk_API.entities.Comments;
import com.portfolio.HelpDesk_API.entities.Status;
import com.portfolio.HelpDesk_API.entities.Ticket;
import com.portfolio.HelpDesk_API.entities.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    private final CommentsRepository commentsRepository;
    private final TicketAccessService ticketAccessService;

    public CommentService(CommentsRepository commentsRepository, TicketAccessService ticketAccessService) {
        this.commentsRepository = commentsRepository;
        this.ticketAccessService = ticketAccessService;
    }

    public CommentsResponseDTO criar(CommentsRequestDTO inputData){
        Ticket ticket = ticketAccessService.findTicketWithAccessCheck(inputData.ticketId());

        if (ticket.getStatus() == Status.CLOSED){
            throw new BusinessRuleException("Não é possível adicionar comentários a um ticket fechado.");
        }

        User user = SecurityUtils.getCurrentUser();
        Comments comments = new Comments(inputData);
        comments.setTicket(ticket);
        comments.setUser(user);
        Comments commentsSalva = commentsRepository.save(comments);
        return new CommentsResponseDTO(commentsSalva);
    }

    public List<CommentsResponseDTO> listByTicket(Long ticketId) {
        Ticket ticket = ticketAccessService.findTicketWithAccessCheck(ticketId);
        List<Comments> comments = commentsRepository.findByTicketId(ticket.getId());
        return comments.stream()
                .map(CommentsResponseDTO::new)
                .toList();
    }
}
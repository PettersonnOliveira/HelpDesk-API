package com.portfolio.HelpDesk_API.Services;

import com.portfolio.HelpDesk_API.Exceptions.BusinessRuleException;
import com.portfolio.HelpDesk_API.Exceptions.ResourceNotFoundException;
import com.portfolio.HelpDesk_API.Repositories.CommentsRepository;
import com.portfolio.HelpDesk_API.Repositories.TicketRepository;
import com.portfolio.HelpDesk_API.Repositories.UserRepository;
import com.portfolio.HelpDesk_API.dtos.CommentsRequestDTO;
import com.portfolio.HelpDesk_API.dtos.CommentsResponseDTO;
import com.portfolio.HelpDesk_API.entities.Comments;
import com.portfolio.HelpDesk_API.entities.Status;
import com.portfolio.HelpDesk_API.entities.Ticket;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class CommentService {
    private final CommentsRepository commentsRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public CommentService(CommentsRepository commentsRepository, TicketRepository ticketRepository, UserRepository userRepository) {
        this.commentsRepository = commentsRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    public CommentsResponseDTO criar(CommentsRequestDTO inputData){
        Long ticketId = inputData.ticketId();

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket não encontrado"));

        if (ticket.getStatus() == Status.CLOSED){
            throw new BusinessRuleException("Não é possível adicionar comentários a um ticket fechado.");
        }

        Comments comments = new Comments(inputData);
        comments.setTicket(ticket);
        Comments commentsSalva = commentsRepository.save(comments);
        return new CommentsResponseDTO(commentsSalva);
    }

    public List<CommentsResponseDTO> listByTicket(Long ticketId) {
        List<Comments> comments = commentsRepository.findByTicketId(ticketId);
        return comments.stream()
                .map(CommentsResponseDTO::new)
                .toList();
    }
}

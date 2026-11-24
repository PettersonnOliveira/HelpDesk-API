package com.portfolio.HelpDesk_API.Services;

import com.portfolio.HelpDesk_API.Exceptions.BusinessRuleException;
import com.portfolio.HelpDesk_API.Exceptions.ResourceNotFoundException;
import com.portfolio.HelpDesk_API.Repositories.TicketRepository;
import com.portfolio.HelpDesk_API.Security.SecurityUtils;
import com.portfolio.HelpDesk_API.dtos.TicketRequestDTO;
import com.portfolio.HelpDesk_API.dtos.TicketResponseDTO;
import com.portfolio.HelpDesk_API.dtos.TicketUpdateDTO;
import com.portfolio.HelpDesk_API.entities.Status;
import com.portfolio.HelpDesk_API.entities.Ticket;
import com.portfolio.HelpDesk_API.entities.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public TicketResponseDTO criar(TicketRequestDTO inputData){
        User user = SecurityUtils.getCurrentUser();

        Ticket ticket = new Ticket(inputData);
        ticket.setUser(user);
        Ticket ticketSalva = ticketRepository.save(ticket);
        return new TicketResponseDTO(ticketSalva);
    }

    public List<TicketResponseDTO> listAll(){
        User user = SecurityUtils.getCurrentUser();
        List<Ticket> tickets = SecurityUtils.isStaff()
                ? ticketRepository.findAll()
                : ticketRepository.findByUserId(user.getId());
        return tickets.stream()
                .map(TicketResponseDTO::new)
                .toList();
    }

    public TicketResponseDTO findById(Long id){
        Ticket ticket = findTicketWithAccessCheck(id);
        return new TicketResponseDTO(ticket);
    }

    public TicketResponseDTO update(Long id, TicketUpdateDTO inputData){
        Ticket ticket = findTicketWithAccessCheck(id);

        if (inputData.title() != null && !inputData.title().isBlank()) {
            ticket.setTitle(inputData.title());
        }
        if (inputData.description() != null) {
            ticket.setDescription(inputData.description());
        }
        if (inputData.status() != null) {
            if (!SecurityUtils.isStaff()) {
                throw new BusinessRuleException("Apenas administradores ou técnicos podem alterar o status do chamado.");
            }
            ticket.setStatus(inputData.status());
        }

        Ticket ticketUpdated = ticketRepository.save(ticket);
        return new TicketResponseDTO(ticketUpdated);
    }

    public void delete(Long id){
        Ticket ticket = findTicketWithAccessCheck(id);
        User user = SecurityUtils.getCurrentUser();
        boolean isOwner = ticket.getUser().getId().equals(user.getId());

        if (!SecurityUtils.isStaff() && (!isOwner || ticket.getStatus() != Status.OPEN)) {
            throw new BusinessRuleException("Clientes só podem excluir chamados próprios ainda abertos.");
        }

        ticketRepository.delete(ticket);
    }

    public List<TicketResponseDTO> findByStatus(Status status){
        User user = SecurityUtils.getCurrentUser();
        List<Ticket> tickets = SecurityUtils.isStaff()
                ? ticketRepository.findByStatus(status)
                : ticketRepository.findByStatusAndUserId(status, user.getId());
        return tickets.stream()
                .map(TicketResponseDTO::new)
                .toList();
    }

    private Ticket findTicketWithAccessCheck(Long id){
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket não encontrado"));
        SecurityUtils.requireTicketAccess(ticket);
        return ticket;
    }
}

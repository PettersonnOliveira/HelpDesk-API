package com.portfolio.HelpDesk_API.Services;

import com.portfolio.HelpDesk_API.Exceptions.ResourceNotFoundException;
import com.portfolio.HelpDesk_API.Repositories.TicketRepository;
import com.portfolio.HelpDesk_API.Repositories.UserRepository;
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
    private final UserRepository userRepository;

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    public TicketResponseDTO criar(TicketRequestDTO inputData){
        Long userId = inputData.userId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User não encontrado"));

        Ticket ticket = new Ticket(inputData);
        ticket.setUser(user);
        Ticket ticketSalva = ticketRepository.save(ticket);
        return new TicketResponseDTO(ticketSalva);

    }
    public List<TicketResponseDTO> listAll(){
        List<Ticket> tickets = ticketRepository.findAll();
        return tickets.stream()
                .map(TicketResponseDTO::new)
                .toList();
    }
    public TicketResponseDTO findById(Long id){
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket não encontrado"));

        return new TicketResponseDTO(ticket);
    }
    public TicketResponseDTO update(Long id, TicketUpdateDTO inputData){
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket não encontrado"));
        ticket.setTitle(inputData.title());
        ticket.setDescription(inputData.description());
        ticket.setStatus(inputData.status());

        Ticket ticketUpdated = ticketRepository.save(ticket);
        return new TicketResponseDTO(ticketUpdated);

    }

    public List<TicketResponseDTO> findByStatus(Status status){
        List<Ticket> tickets = ticketRepository.findByStatus(status);
        return tickets.stream()
                .map(TicketResponseDTO::new)
                .toList();

    }
}

package com.portfolio.HelpDesk_API;

import com.portfolio.HelpDesk_API.Exceptions.BusinessRuleException;
import com.portfolio.HelpDesk_API.Exceptions.ResourceNotFoundException;
import com.portfolio.HelpDesk_API.Repositories.TicketRepository;
import com.portfolio.HelpDesk_API.Services.TicketService;
import com.portfolio.HelpDesk_API.dtos.TicketRequestDTO;
import com.portfolio.HelpDesk_API.dtos.TicketResponseDTO;
import com.portfolio.HelpDesk_API.dtos.TicketUpdateDTO;
import com.portfolio.HelpDesk_API.entities.Role;
import com.portfolio.HelpDesk_API.entities.Status;
import com.portfolio.HelpDesk_API.entities.Ticket;
import com.portfolio.HelpDesk_API.entities.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TicketServiceTest {

    private TicketRepository ticketRepository;
    private TicketService ticketService;

    private User client;
    private User admin;

    @BeforeEach
    void setUp() {
        ticketRepository = mock(TicketRepository.class);
        ticketService = new TicketService(ticketRepository);

        client = new User("Cliente", "cliente@mail.com", "123456", Role.CLIENT);
        client.setId(1L);
        admin = new User("Admin", "admin@mail.com", "123456", Role.ADMIN);
        admin.setId(2L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(User user) {
        var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private Ticket ticketDoCliente(Status status) {
        return new Ticket("Problema", "Descricao", status, LocalDateTime.now(), client);
    }

    @Test
    void criarDeveIgnorarUserIdDoBodyEVincularUsuarioAutenticado() {
        authenticateAs(client);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketRequestDTO dto = new TicketRequestDTO("Titulo", "Desc", 999L);
        TicketResponseDTO response = ticketService.criar(dto);

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getUser().getId());
        assertEquals(client.getId(), response.user().id());
    }

    @Test
    void listAllComoClienteRetornaApenasTicketsProprios() {
        authenticateAs(client);
        Ticket own = ticketDoCliente(Status.OPEN);
        own.setId(10L);
        when(ticketRepository.findByUserId(1L)).thenReturn(List.of(own));

        List<TicketResponseDTO> result = ticketService.listAll();

        assertEquals(1, result.size());
        verify(ticketRepository, never()).findAll();
    }

    @Test
    void listAllComoAdminRetornaTodosOsTickets() {
        authenticateAs(admin);
        when(ticketRepository.findAll()).thenReturn(List.of(ticketDoCliente(Status.OPEN)));

        List<TicketResponseDTO> result = ticketService.listAll();

        assertEquals(1, result.size());
        verify(ticketRepository, never()).findByUserId(any());
    }

    @Test
    void findByIdDeOutroClienteLancaExcecao() {
        authenticateAs(client);
        User outro = new User("Outro", "outro@mail.com", "123456", Role.CLIENT);
        outro.setId(99L);
        Ticket ticketAlheio = new Ticket("X", "Y", Status.OPEN, LocalDateTime.now(), outro);
        ticketAlheio.setId(5L);
        when(ticketRepository.findById(5L)).thenReturn(Optional.of(ticketAlheio));

        assertThrows(BusinessRuleException.class, () -> ticketService.findById(5L));
    }

    @Test
    void updateNaoSobrescreveCamposNulos() {
        authenticateAs(admin);
        Ticket ticket = ticketDoCliente(Status.OPEN);
        ticket.setId(7L);
        ticket.setTitle("Titulo Original");
        ticket.setDescription("Desc Original");
        when(ticketRepository.findById(7L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketResponseDTO response = ticketService.update(7L, new TicketUpdateDTO(null, null, Status.IN_PROGRESS));

        assertEquals("Titulo Original", response.title());
        assertEquals("Desc Original", response.description());
        assertEquals(Status.IN_PROGRESS, response.status());
    }

    @Test
    void updateDeStatusComoClienteLancaExcecao() {
        authenticateAs(client);
        Ticket ticket = ticketDoCliente(Status.OPEN);
        ticket.setId(7L);
        when(ticketRepository.findById(7L)).thenReturn(Optional.of(ticket));

        assertThrows(BusinessRuleException.class,
                () -> ticketService.update(7L, new TicketUpdateDTO(null, null, Status.CLOSED)));
    }

    @Test
    void updateTituloComoDonoFunciona() {
        authenticateAs(client);
        Ticket ticket = ticketDoCliente(Status.OPEN);
        ticket.setId(7L);
        when(ticketRepository.findById(7L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketResponseDTO response = ticketService.update(7L, new TicketUpdateDTO("Novo titulo", null, null));

        assertEquals("Novo titulo", response.title());
        assertEquals(Status.OPEN, response.status());
    }

    @Test
    void deleteComoClienteEmTicketFechadoLancaExcecao() {
        authenticateAs(client);
        Ticket ticket = ticketDoCliente(Status.CLOSED);
        ticket.setId(7L);
        when(ticketRepository.findById(7L)).thenReturn(Optional.of(ticket));

        assertThrows(BusinessRuleException.class, () -> ticketService.delete(7L));
        verify(ticketRepository, never()).delete(any(Ticket.class));
    }

    @Test
    void deleteComoAdminRemoveQualquerTicket() {
        authenticateAs(admin);
        Ticket ticket = ticketDoCliente(Status.CLOSED);
        ticket.setId(7L);
        when(ticketRepository.findById(7L)).thenReturn(Optional.of(ticket));

        ticketService.delete(7L);

        verify(ticketRepository).delete(ticket);
    }

    @Test
    void findByIdInexistenteLancaResourceNotFound() {
        authenticateAs(client);
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ticketService.findById(999L));
    }
}

package com.portfolio.HelpDesk_API;

import com.portfolio.HelpDesk_API.Exceptions.BusinessRuleException;
import com.portfolio.HelpDesk_API.Repositories.CommentsRepository;
import com.portfolio.HelpDesk_API.Repositories.TicketRepository;
import com.portfolio.HelpDesk_API.Services.CommentService;
import com.portfolio.HelpDesk_API.dtos.CommentsRequestDTO;
import com.portfolio.HelpDesk_API.dtos.CommentsResponseDTO;
import com.portfolio.HelpDesk_API.entities.Comments;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CommentServiceTest {

    private CommentsRepository commentsRepository;
    private TicketRepository ticketRepository;
    private CommentService commentService;

    private User client;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        commentsRepository = mock(CommentsRepository.class);
        ticketRepository = mock(TicketRepository.class);
        commentService = new CommentService(commentsRepository, ticketRepository);

        client = new User("Cliente", "cliente@mail.com", "123456", Role.CLIENT);
        client.setId(1L);
        ticket = new Ticket("Problema", "Desc", Status.OPEN, LocalDateTime.now(), client);
        ticket.setId(10L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(User user) {
        var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void criarVinculaUsuarioAutenticado() {
        authenticateAs(client);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(commentsRepository.save(any(Comments.class))).thenAnswer(inv -> inv.getArgument(0));

        CommentsResponseDTO response = commentService.criar(new CommentsRequestDTO("Nova atualizacao", 10L));

        ArgumentCaptor<Comments> captor = ArgumentCaptor.forClass(Comments.class);
        verify(commentsRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getUser().getId());
        assertEquals("Nova atualizacao", response.message());
        assertEquals(1L, response.user().id());
    }

    @Test
    void criarEmTicketFechadoLancaExcecao() {
        authenticateAs(client);
        ticket.setStatus(Status.CLOSED);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        assertThrows(BusinessRuleException.class,
                () -> commentService.criar(new CommentsRequestDTO("msg", 10L)));
    }

    @Test
    void criarEmTicketDeOutroClienteLancaExcecao() {
        authenticateAs(client);
        User outro = new User("Outro", "outro@mail.com", "123456", Role.CLIENT);
        outro.setId(99L);
        Ticket ticketAlheio = new Ticket("X", "Y", Status.OPEN, LocalDateTime.now(), outro);
        ticketAlheio.setId(20L);
        when(ticketRepository.findById(20L)).thenReturn(Optional.of(ticketAlheio));

        assertThrows(BusinessRuleException.class,
                () -> commentService.criar(new CommentsRequestDTO("msg", 20L)));
    }
}

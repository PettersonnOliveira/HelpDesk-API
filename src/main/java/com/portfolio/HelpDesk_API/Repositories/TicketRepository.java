package com.portfolio.HelpDesk_API.Repositories;

import com.portfolio.HelpDesk_API.entities.Status;
import com.portfolio.HelpDesk_API.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByStatus(Status status);
    List<Ticket> findByUserId(Long userId);
    List<Ticket> findByStatusAndUserId(Status status, Long userId);
}

package com.portfolio.HelpDesk_API.Repositories;

import com.portfolio.HelpDesk_API.entities.Comments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentsRepository extends JpaRepository<Comments, Long> {
    List<Comments> findByTicketId(Long ticketId);

}

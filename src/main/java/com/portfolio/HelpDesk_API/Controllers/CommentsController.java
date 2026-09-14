package com.portfolio.HelpDesk_API.Controllers;

import com.portfolio.HelpDesk_API.Services.CommentService;
import com.portfolio.HelpDesk_API.dtos.CommentsRequestDTO;
import com.portfolio.HelpDesk_API.dtos.CommentsResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("comments")
public class CommentsController {
    private final CommentService commentService;

    public CommentsController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<CommentsResponseDTO> criar(@RequestBody CommentsRequestDTO inputData) {
        CommentsResponseDTO commentsResponseDTO = commentService.criar(inputData);
        return ResponseEntity.status(HttpStatus.CREATED).body(commentsResponseDTO);
    }

    @GetMapping("/tickets/{id}/comments")
    public ResponseEntity<List<CommentsResponseDTO>> listByTicket(
            @PathVariable Long id) {
        List<CommentsResponseDTO> comments = commentService.listByTicket(id);
        return ResponseEntity.ok(comments);
    }
}

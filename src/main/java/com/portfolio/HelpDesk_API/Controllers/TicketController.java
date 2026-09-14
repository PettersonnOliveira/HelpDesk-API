package com.portfolio.HelpDesk_API.Controllers;

import com.portfolio.HelpDesk_API.Services.TicketService;
import com.portfolio.HelpDesk_API.dtos.TicketRequestDTO;
import com.portfolio.HelpDesk_API.dtos.TicketResponseDTO;
import com.portfolio.HelpDesk_API.dtos.TicketUpdateDTO;
import com.portfolio.HelpDesk_API.entities.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }
    @PostMapping
    public ResponseEntity<TicketResponseDTO> criar(@RequestBody TicketRequestDTO inputData){
        TicketResponseDTO ticketResponseDTO = ticketService.criar(inputData);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketResponseDTO);
    }

    @GetMapping
    public ResponseEntity<List<TicketResponseDTO>> listAll(){
        List<TicketResponseDTO> tickets = ticketService.listAll();
        return ResponseEntity.ok(tickets);

    }
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> findById(@PathVariable Long id){
        TicketResponseDTO ticketResponseDTO = ticketService.findById(id);
        return ResponseEntity.ok(ticketResponseDTO);
    }
    @PutMapping("/{id}")
        public ResponseEntity<TicketResponseDTO> update (@PathVariable Long id, @RequestBody TicketUpdateDTO inputData){
        TicketResponseDTO ticketResponseDTO = ticketService.update(id,inputData);
        return ResponseEntity.ok(ticketResponseDTO);
    }
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TicketResponseDTO>> findByStatus(@PathVariable Status status){
        List<TicketResponseDTO> ticketResponseDTO = ticketService.findByStatus(status);
        return ResponseEntity.ok(ticketResponseDTO);

    }
}

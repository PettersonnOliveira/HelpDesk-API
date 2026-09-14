package com.portfolio.HelpDesk_API.dtos;

import com.portfolio.HelpDesk_API.entities.Status;

public record TicketUpdateDTO( String title,
                               String description,
                               Status status
){

}

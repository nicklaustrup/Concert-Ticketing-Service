package com.kenzie.unit.four.ticketsystem.controller;

import com.kenzie.unit.four.ticketsystem.controller.model.ConcertResponse;
import com.kenzie.unit.four.ticketsystem.controller.model.ReservedTicketCreateRequest;
import com.kenzie.unit.four.ticketsystem.controller.model.ReservedTicketResponse;
import com.kenzie.unit.four.ticketsystem.service.ReservedTicketService;
import com.kenzie.unit.four.ticketsystem.service.model.Concert;
import com.kenzie.unit.four.ticketsystem.service.model.ReservedTicket;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.UUID.randomUUID;

@RestController
@RequestMapping("/reservedtickets")
public class ReservedTicketController {

    private ReservedTicketService reservedTicketService;

    ReservedTicketController(ReservedTicketService reservedTicketService) {
        this.reservedTicketService = reservedTicketService;
    }

    @PostMapping
    public ResponseEntity<ReservedTicketResponse> reserveTicket(
            @RequestBody ReservedTicketCreateRequest reservedTicketCreateRequest) {

        // Add your code here
        ReservedTicket reservedTicket = new ReservedTicket(reservedTicketCreateRequest.getConcertId(),
                randomUUID().toString(),
                LocalDateTime.now().toString());

        reservedTicketService.reserveTicket(reservedTicket);

        ReservedTicketResponse ticketResponse = createReservedTicketResponse(reservedTicket);

        // Return your ReservedTicketResponse instead of null
        return ResponseEntity.ok(ticketResponse);
    }

    @GetMapping("/concerts/{concertId}")
    public ResponseEntity<List<ReservedTicketResponse>> getAllReserveTicketsByConcertId(
            @PathVariable("concertId") String concertId) {

        // Retrieve reserved ticket list from ticket service using path variable concertId
        List<ReservedTicket> reservedTicketList = reservedTicketService.findByConcertId(concertId);

        //check if returned list is null or empty and send no content status
        if (reservedTicketList == null || reservedTicketList.isEmpty()){
            return ResponseEntity.noContent().build();
        }

        //create list for reserved ticket response
        List<ReservedTicketResponse> ticketResponseList = new ArrayList<>();

        //create a ticket response from each reserved ticket and add to ticket response list
        for (ReservedTicket ticket: reservedTicketList) {
            ticketResponseList.add(createReservedTicketResponse(ticket));
        }

        // Return List<ReservedTicketResponse> with status code
        return ResponseEntity.ok(ticketResponseList);
    }

    private ReservedTicketResponse createReservedTicketResponse(ReservedTicket ticket) {
        ReservedTicketResponse reservedTicketResponse = new ReservedTicketResponse();
        reservedTicketResponse.setConcertId(ticket.getConcertId());
        reservedTicketResponse.setTicketId(ticket.getTicketId());
        reservedTicketResponse.setDateOfReservation(ticket.getDateOfReservation());
        reservedTicketResponse.setReservationClosed(ticket.getReservationClosed());
        reservedTicketResponse.setPurchasedTicket(ticket.getTicketPurchased());
        return reservedTicketResponse;
    }
}

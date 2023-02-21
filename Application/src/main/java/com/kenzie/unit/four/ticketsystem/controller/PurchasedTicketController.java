package com.kenzie.unit.four.ticketsystem.controller;

import com.kenzie.unit.four.ticketsystem.controller.model.PurchasedTicketCreateRequest;
import com.kenzie.unit.four.ticketsystem.controller.model.PurchasedTicketResponse;
import com.kenzie.unit.four.ticketsystem.service.PurchasedTicketService;
import com.kenzie.unit.four.ticketsystem.service.ReservedTicketService;
import com.kenzie.unit.four.ticketsystem.service.model.PurchasedTicket;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/purchasedtickets")
public class PurchasedTicketController {

    private PurchasedTicketService purchasedTicketService;
    private ReservedTicketService reservedTicketService;


    PurchasedTicketController(PurchasedTicketService purchasedTicketService, 
        ReservedTicketService reservedTicketService) {
        this.purchasedTicketService = purchasedTicketService;
        this.reservedTicketService = reservedTicketService;
    }

  @PostMapping
    public ResponseEntity<PurchasedTicketResponse> purchaseTicket(
            @RequestBody PurchasedTicketCreateRequest purchasedTicketCreateRequest) {

        PurchasedTicket purchaseTicket = purchasedTicketService.purchaseTicket(purchasedTicketCreateRequest.getTicketId(), purchasedTicketCreateRequest.getPricePaid());
        PurchasedTicketResponse purchasedTicketResponse = createPurchaseTicketResponse(purchaseTicket);

        // Return your ReservedTicketResponse instead of null
        return ResponseEntity.ok(purchasedTicketResponse);
    }

    @GetMapping("/concerts/{concertId}")
    public ResponseEntity<List<PurchasedTicketResponse>>
        getAllPurchaseTicketsByConcertId(@PathVariable("concertId") String concertId) {
        List<PurchasedTicket> purchasedTickets = purchasedTicketService.findByConcertId(concertId);
        if (purchasedTickets == null || purchasedTickets.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<PurchasedTicketResponse> tickets = new ArrayList<>();

        for (PurchasedTicket purchasedTicket : purchasedTickets) {
            tickets.add(createPurchaseTicketResponse(purchasedTicket));
        }

        return ResponseEntity.ok(tickets);
    }

    private PurchasedTicketResponse createPurchaseTicketResponse(PurchasedTicket purchasedTicket) {
        PurchasedTicketResponse purchasedTicketResponse = new PurchasedTicketResponse();
        purchasedTicketResponse.setTicketId(purchasedTicket.getTicketId());
        purchasedTicketResponse.setDateOfPurchase(purchasedTicket.getDateOfPurchase());
        purchasedTicketResponse.setPricePaid(purchasedTicket.getPricePaid());
        return purchasedTicketResponse;
    }
}

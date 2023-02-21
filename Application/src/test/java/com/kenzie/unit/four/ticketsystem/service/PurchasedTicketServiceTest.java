package com.kenzie.unit.four.ticketsystem.service;

import com.kenzie.unit.four.ticketsystem.repositories.PurchaseTicketRepository;
import com.kenzie.unit.four.ticketsystem.repositories.model.PurchasedTicketRecord;
import com.kenzie.unit.four.ticketsystem.repositories.model.ReserveTicketRecord;
import com.kenzie.unit.four.ticketsystem.service.model.PurchasedTicket;
import com.kenzie.unit.four.ticketsystem.service.model.ReservedTicket;
import jdk.vm.ci.meta.Local;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.*;

public class PurchasedTicketServiceTest {
    private PurchaseTicketRepository purchaseTicketRepository;
    private ReservedTicketService reservedTicketService;
    private PurchasedTicketService purchasedTicketService;

    @BeforeEach
    void setup() {
        purchaseTicketRepository = mock(PurchaseTicketRepository.class);
        reservedTicketService = mock(ReservedTicketService.class);
        purchasedTicketService = new PurchasedTicketService(purchaseTicketRepository, reservedTicketService);
    }

    /** ------------------------------------------------------------------------
     *  purchasedTicketService.purchaseTicket
     *  ------------------------------------------------------------------------ **/

    @Test
    void purchaseTicket_successful() throws InterruptedException {
        String concertId = randomUUID().toString();
        String ticketId = randomUUID().toString();
        String time = Clock.fixed(Instant.now(), ZoneId.systemDefault()).toString();

        ReserveTicketRecord record = new ReserveTicketRecord();
        record.setTicketId(ticketId);
        record.setConcertId(concertId);
        record.setDateOfReservation(time);
        record.setDateReservationClosed(null);
        record.setReservationClosed(null);
        record.setPurchasedTicket(true);

        ReservedTicket reservedTicket = new ReservedTicket(
                record.getConcertId(),
                record.getTicketId(),
                record.getDateOfReservation(),
                record.getReservationClosed(),
                record.getDateReservationClosed(),
                record.getPurchasedTicket());

        reservedTicketService.reserveTicket(reservedTicket);

        when(reservedTicketService.findByReserveTicketId(any())).thenReturn(reservedTicket);


        PurchasedTicketRecord purchasedTicketRecord = new PurchasedTicketRecord();
        purchasedTicketRecord.setTicketId(ticketId);
        purchasedTicketRecord.setDateOfPurchase(LocalDateTime.now().toString());
        purchasedTicketRecord.setConcertId(reservedTicket.getConcertId());
        purchasedTicketRecord.setPricePaid(90.0);

        ArgumentCaptor<PurchasedTicketRecord> recordCaptor = ArgumentCaptor.forClass(PurchasedTicketRecord.class);

        // WHEN
        purchasedTicketService.purchaseTicket(ticketId, 90.0);

        // THEN
        verify(purchaseTicketRepository).save(recordCaptor.capture());
        PurchasedTicketRecord storedRecord = recordCaptor.getValue();

        Assertions.assertNotNull(reservedTicket);
        Assertions.assertEquals(storedRecord.getConcertId(), reservedTicket.getConcertId(), "The concert id matches");
    }

    @Test
    void purchaseTicket_reservedTicketNull_throwsResponseStatusException(){
        when(reservedTicketService.findByReserveTicketId(any())).thenReturn(null);

        // WHEN
        Assertions.assertThrows(ResponseStatusException.class,
                ()-> purchasedTicketService.purchaseTicket(randomUUID().toString(), 90.0));
    }


    /** ------------------------------------------------------------------------
     *  purchasedTicketService.findByConcertId
     *  ------------------------------------------------------------------------ **/

    @Test
    void findByConcertId() {
        // GIVEN
        String concertId = randomUUID().toString();

        PurchasedTicketRecord record = new PurchasedTicketRecord();
        record.setConcertId(concertId);
        record.setTicketId(randomUUID().toString());
        record.setDateOfPurchase("purchasedate");
        record.setPricePaid(11.0);

        // WHEN
        when(purchaseTicketRepository.findByConcertId(concertId)).thenReturn(Arrays.asList(record));
        List<PurchasedTicket> purchasedTickets = purchasedTicketService.findByConcertId(concertId);

        // THEN
        Assertions.assertEquals(1, purchasedTickets.size(), "There is one Purchased Ticket");
        PurchasedTicket ticket = purchasedTickets.get(0);
        Assertions.assertNotNull(ticket, "The purchased ticket is returned");
        Assertions.assertEquals(record.getConcertId(), ticket.getConcertId(), "The concert id matches");
        Assertions.assertEquals(record.getTicketId(), ticket.getTicketId(), "The ticket id matches");
        Assertions.assertEquals(record.getDateOfPurchase(), ticket.getDateOfPurchase(), "The date of purchase matches");
        Assertions.assertEquals(record.getPricePaid(), ticket.getPricePaid(), "The price paid matches");
    }

    @Test
    void findByConcertId_no_purchased_tickets() {
        // GIVEN
        String concertId = randomUUID().toString();

        // WHEN
        when(purchaseTicketRepository.findByConcertId(concertId)).thenReturn(new ArrayList<PurchasedTicketRecord>());
        List<PurchasedTicket> purchasedTickets = purchasedTicketService.findByConcertId(concertId);

        // THEN
        Assertions.assertEquals(0, purchasedTickets.size(), "There are no Purchased Tickets");
    }
}

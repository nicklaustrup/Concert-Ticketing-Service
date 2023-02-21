package com.kenzie.unit.four.ticketsystem.service;

import com.kenzie.unit.four.ticketsystem.repositories.ReservedTicketRepository;
import com.kenzie.unit.four.ticketsystem.repositories.model.ReserveTicketRecord;
import com.kenzie.unit.four.ticketsystem.service.model.ReservedTicket;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;


@Service
public class ReservedTicketService {
    private ConcertService concertService;
    private ReservedTicketRepository reservedTicketRepository;
    private final ConcurrentLinkedQueue<ReservedTicket> reservedTicketsQueue;

    public ReservedTicketService(ReservedTicketRepository reservedTicketRepository,
                                 ConcertService concertService,
                                 ConcurrentLinkedQueue<ReservedTicket> reservedTicketsQueue) {
        this.reservedTicketRepository = reservedTicketRepository;
        this.concertService = concertService;
        this.reservedTicketsQueue = reservedTicketsQueue;
    }

    public List<ReservedTicket> findAllReservationTickets() {
        List<ReservedTicket> reservedTickets = new ArrayList<>();

        Iterable<ReserveTicketRecord> ticketRecords = reservedTicketRepository.findAll();

        for (ReserveTicketRecord record : ticketRecords) {
            reservedTickets.add(new ReservedTicket(record.getConcertId(),
                    record.getTicketId(),
                    record.getDateOfReservation(),
                    record.getReservationClosed(),
                    record.getDateReservationClosed(),
                    record.getPurchasedTicket()));
        }


        return reservedTickets;
    }

    public List<ReservedTicket> findAllUnclosedReservationTickets() {
        List<ReservedTicket> allReservedTickets = findAllReservationTickets();
        List<ReservedTicket> unclosedReservations = new ArrayList<>();

        for (ReservedTicket reservedTicket : allReservedTickets) {
            if ((reservedTicket.getTicketPurchased() == null || !reservedTicket.getTicketPurchased()) &&
                (reservedTicket.getReservationClosed() == null || !reservedTicket.getReservationClosed())) {
                unclosedReservations.add(reservedTicket);
            }
        }
        return unclosedReservations;
    }

    public ReservedTicket reserveTicket(ReservedTicket reservedTicket) {
        //Check if concert exists
        if (concertService.findByConcertId(reservedTicket.getConcertId()) == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Concert ID is invalid.");
        }
        //Create Reserved Ticket Record
        ReserveTicketRecord reserveTicketRecord = new ReserveTicketRecord();
        reserveTicketRecord.setConcertId(reservedTicket.getConcertId());
        reserveTicketRecord.setTicketId(reservedTicket.getTicketId());
        reserveTicketRecord.setDateOfReservation(reservedTicket.getDateOfReservation());

        //Save reserved ticket
        reservedTicketRepository.save(reserveTicketRecord);

        //Add reservedTicket to Que
        reservedTicketsQueue.add(reservedTicket);

        return reservedTicket;
    }

    public ReservedTicket findByReserveTicketId(String reserveTicketId) {
        Optional<ReserveTicketRecord> ticketRecordOptional = reservedTicketRepository.findById(reserveTicketId);
        if (ticketRecordOptional.isPresent()) {
            ReserveTicketRecord record = ticketRecordOptional.get();
            return new ReservedTicket(record.getConcertId(),
                    record.getTicketId(),
                    record.getDateOfReservation(),
                    record.getReservationClosed(),
                    record.getDateReservationClosed(),
                    record.getPurchasedTicket());
        } else {
            return null;
        }
    }

    public List<ReservedTicket> findByConcertId(String concertId) {
        //Empty arraylist for reserved tickets
        List<ReservedTicket> tickets = new ArrayList<>();

        //Retrieve reserved ticket record list from repository
        List<ReserveTicketRecord> records = reservedTicketRepository.findByConcertId(concertId);

        //loop through record list, retrieve the reserved ticket object by id lookup,
        // and add reserved tickets to reserved ticket array
        for (ReserveTicketRecord record: records) {
            tickets.add(findByReserveTicketId(record.getTicketId()));
        }
        return tickets;
    }

    public ReservedTicket updateReserveTicket(ReservedTicket reservedTicket) {
        ReserveTicketRecord reserveTicketRecord = new ReserveTicketRecord();
        reserveTicketRecord.setTicketId(reservedTicket.getTicketId());
        reserveTicketRecord.setConcertId(reservedTicket.getConcertId());
        reserveTicketRecord.setDateOfReservation(reservedTicket.getDateOfReservation());
        reserveTicketRecord.setDateReservationClosed(reservedTicket.getDateReservationClosed());
        reserveTicketRecord.setReservationClosed(reservedTicket.getReservationClosed());
        reserveTicketRecord.setPurchasedTicket(reservedTicket.getTicketPurchased());
        reservedTicketRepository.save(reserveTicketRecord);
        return reservedTicket;
    }
}

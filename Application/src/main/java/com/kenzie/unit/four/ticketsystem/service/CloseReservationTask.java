package com.kenzie.unit.four.ticketsystem.service;

import com.kenzie.unit.four.ticketsystem.repositories.model.ReserveTicketRecord;
import com.kenzie.unit.four.ticketsystem.service.model.ReservedTicket;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CloseReservationTask implements Runnable {

    private final Integer durationToPay;
    private final ConcurrentLinkedQueue<ReservedTicket> reservedTicketsQueue;
    private final ReservedTicketService reservedTicketService;

    public CloseReservationTask(Integer durationToPay,
                                ReservedTicketService reservedTicketService,
                                ConcurrentLinkedQueue<ReservedTicket> reservedTicketsQueue) {
        this.durationToPay = durationToPay;
        this.reservedTicketService = reservedTicketService;
        this.reservedTicketsQueue = reservedTicketsQueue;
    }

    @Override
    public void run() {

        for (ReservedTicket reservedTicket : reservedTicketsQueue){
            ReservedTicket reservedTicketFromRepo = reservedTicketService.findByReserveTicketId(reservedTicket.getTicketId());
            LocalDateTime reservationTime = LocalDateTime.parse(reservedTicketFromRepo.getDateOfReservation());
            Duration duration = Duration.between(reservationTime, LocalDateTime.now());


            if (duration.getSeconds() > durationToPay && reservedTicketFromRepo.getTicketPurchased() == null) {

                ReservedTicket unpurchasedTicket = new ReservedTicket(
                        reservedTicketFromRepo.getConcertId(),
                        reservedTicketFromRepo.getTicketId(),
                        reservedTicketFromRepo.getDateOfReservation(),
                        true,
                        LocalDateTime.now().toString(),
                        false
                );

                reservedTicketService.updateReserveTicket(unpurchasedTicket);

            } else  if (duration.getSeconds() < durationToPay && reservedTicketFromRepo.getTicketPurchased()) {

                reservedTicketsQueue.add(reservedTicketFromRepo);

            }

        }
    }
}


package p1.client.server.controller;

import p1.client.server.domain.exception.ControllerException;
import p1.client.server.domain.model.Seat;
import p1.client.server.domain.model.Spectacle;
import p1.client.server.domain.model.Ticket;
import p1.client.server.repository.SeatRepositoryImpl;
import p1.client.server.repository.SpectacleRepositoryImpl;
import p1.client.server.repository.TicketRepositoryImpl;
import p1.client.server.utils.Constants;
import p1.client.server.utils.GUID;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class Controller {
    private final SpectacleRepositoryImpl spectacleRepository;
    private final TicketRepositoryImpl ticketRepository;
    private final SeatRepositoryImpl seatRepository;

    public Controller(SpectacleRepositoryImpl spectacleRepository, TicketRepositoryImpl ticketRepository, SeatRepositoryImpl seatRepository) {
        this.spectacleRepository = spectacleRepository;
        this.ticketRepository = ticketRepository;
        this.seatRepository = seatRepository;
    }

    /**
     * Reserves a ticket. First it checks if those seats are available to be purchased and if the correct spectacle is
     * specified. After that it creates a new ticket and it tries to save it. If the creation is successful, it updates
     * the balance of the given spectacle.
     *
     * @param seats     the seats to be reserved
     * @param spectacle the spectacle for which the seats are being reserved
     * @return Ticket or null.
     */
    public Ticket reserveTicket(List<Seat> seats, Spectacle spectacle) throws ControllerException {
        seats.forEach(seat -> {
            if (
                    this.seatRepository.findOne(seat.getId()) == null ||
                            !seat.getSpectacle().getId().equals(spectacle.getId())
            ) {
                throw new ControllerException("Couldn't validate seat " + seat.toString());
            }
        });

        Ticket ticket = new Ticket(GUID.get(), OffsetDateTime.now(), seats, spectacle);
        ticket = this.ticketRepository.save(ticket);
        // Couldn't save the ticket
        if (ticket == null) {
            throw new ControllerException("Couldn't save the ticket.");
        }

        Spectacle storedSpectacle = this.spectacleRepository.findOne(spectacle.getId());
        storedSpectacle.setBalance(storedSpectacle.getBalance() + ticket.sum());
        this.spectacleRepository.update(storedSpectacle);
        for (Seat seat : ticket.getSeats()) {
            this.seatRepository.delete(seat.getId());
        }

        return ticket;
    }

    public float sum(Spectacle spectacle) {
        float sum = 0.0f;
        for (Ticket ticket : this.ticketRepository.findAll()) {
            if (spectacle.getId().equals(ticket.getSpectacle().getId())) {
                sum += ticket.sum();
            }
        }

        return sum;
    }

    public int numberOfReservedSeats(Spectacle spectacle) {
        int n = 0;
        for (Ticket ticket : this.ticketRepository.findAll()) {
            if (ticket.getSpectacle().getId().equals(spectacle.getId())) {
                n += ticket.getSeats().size();
            }
        }

        return n;
    }

    public List<Spectacle> findAllSpectacles() {
        return this.spectacleRepository.findAll();
    }

    public List<Seat> findAllSeats() {
        return this.seatRepository.findAll();
    }

    public List<Seat> findAllSeats(Spectacle spectacle) {
        List<Seat> seats = new ArrayList<>();
        this.seatRepository.findAll().forEach(seat -> {
            if (seat.getSpectacle().getId().equals(spectacle.getId())) {
                seats.add(seat);
            }
        });

        return seats;
    }

    public void saveToFile() {
        this.spectacleRepository.saveToFile(Constants.SPECTACLE_FILE);
        this.ticketRepository.saveToFile(Constants.TICKETS_FILE);
        this.seatRepository.saveToFile(Constants.SEATS_FILE);
    }
}

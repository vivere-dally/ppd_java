package p1.client.server.client;

import lombok.extern.slf4j.Slf4j;
import p1.client.server.domain.exception.ClientException;
import p1.client.server.domain.exception.ServerException;
import p1.client.server.domain.model.Seat;
import p1.client.server.domain.model.Spectacle;
import p1.client.server.utils.networking.protocol.request.RequestLogin;
import p1.client.server.utils.networking.protocol.request.RequestLogout;
import p1.client.server.utils.networking.protocol.request.RequestReserveTicket;
import p1.client.server.utils.networking.protocol.response.ResponseLogin;
import p1.client.server.utils.networking.protocol.response.ResponseReserveTicket;
import p1.client.server.utils.networking.service.Observer;
import p1.client.server.utils.networking.service.Service;
import p1.client.server.utils.networking.service.ServiceProxy;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static p1.client.server.utils.Constants.*;

@Slf4j
public class Client implements Runnable, Observer, Serializable {
    private Service service;

    private int id;
    private List<Seat> availableSeats;

    private boolean running;

    public void init() throws ClientException {
        this.service = new ServiceProxy(HOST, SERVER_PORT);
        try {
            ResponseLogin responseLogin = this.service.login(new RequestLogin(), this);
            this.id = responseLogin.getId();
            this.availableSeats = responseLogin.getSeats();
            this.running = true;
            log.info("Logged in successfully!");
        } catch (ServerException e) {
            throw new ClientException("Failed on login!", e);
        }
    }

    @SuppressWarnings("BusyWait")
    @Override
    public void run() {
        this.init();
        while (this.running) {
            //TODO Zombie threads
            try {
                Thread.sleep(CLIENT_REQUEST_OFFSET_IN_MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (this.availableSeats.size() == 0) {
                log.info("All seats were reserved, logging out...");
                try {
                    this.service.logout(new RequestLogout(this.id));
                } catch (ServerException e) {
                    log.info("Couldn't logout...\n{}", e.getMessage());
                } finally {
                    this.running = false;
                }
            }

            Map<Spectacle, List<Seat>> map = this.availableSeats
                    .stream()
                    .collect(Collectors.groupingBy(Seat::getSpectacle));

            for (Map.Entry<Spectacle, List<Seat>> entry : map.entrySet()) {
                int numberOfSeatsToReserve = Math.min(entry.getValue().size(), new Random().nextInt(MAX_NUMBER_OF_SEATS_PER_TICKET));
                if (numberOfSeatsToReserve > 0) {
                    Collections.shuffle(entry.getValue());
                    RequestReserveTicket requestReserveTicket = new RequestReserveTicket(
                            new ArrayList<>(entry.getValue().subList(0, numberOfSeatsToReserve)),
                            entry.getKey()
                    );

                    try {
                        ResponseReserveTicket responseReserveTicket = this.service.reserveTicket(requestReserveTicket);
                        if (responseReserveTicket.getTicket() != null) {
                            log.info("Reserved a new ticket: {}", responseReserveTicket.getTicket());
                        }
                        else {
                            log.info("Reserve ticket request: {} failed!", requestReserveTicket);
                        }
                    } catch (ServerException serverException) {
                        log.info("Reserve ticket request: {} failed!", requestReserveTicket);
                    }
                }
            }
        }
    }

    @Override
    public void onStop() {
        log.info("Stop notification!");
        this.running = false;
    }

    @Override
    public void availableSeats(List<Seat> seats) {
        this.availableSeats = seats;
    }
}

package p1.client.server.server;

import lombok.extern.slf4j.Slf4j;
import p1.client.server.controller.Controller;
import p1.client.server.domain.exception.ServerException;
import p1.client.server.domain.model.Seat;
import p1.client.server.domain.model.Ticket;
import p1.client.server.utils.networking.protocol.request.RequestLogin;
import p1.client.server.utils.networking.protocol.request.RequestLogout;
import p1.client.server.utils.networking.protocol.request.RequestReserveTicket;
import p1.client.server.utils.networking.protocol.response.ResponseLogin;
import p1.client.server.utils.networking.protocol.response.ResponseLogout;
import p1.client.server.utils.networking.protocol.response.ResponseReserveTicket;
import p1.client.server.utils.networking.service.Observer;
import p1.client.server.utils.networking.service.Service;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

import static p1.client.server.utils.Constants.NUMBER_OF_THREAD_POOL_THREADS;

@Slf4j
public class ServiceImpl implements Service {
    private final Controller controller;
    private final ReadWriteLock readWriteLock;

    private final HashMap<Integer, Observer> clients;
    private final ExecutorService executorService;
    private final ReentrantLock loginLogoutLock;
    private int clientIdTracker = 0;

    public ServiceImpl(Controller controller, ReadWriteLock readWriteLock) {
        this.controller = controller;
        this.readWriteLock = readWriteLock;

        this.clients = new HashMap<>();
        this.executorService = Executors.newFixedThreadPool(NUMBER_OF_THREAD_POOL_THREADS);
        this.loginLogoutLock = new ReentrantLock(true);
    }

    @Override
    public ResponseLogin login(RequestLogin requestLogin, Observer observer) throws ServerException {
        int clientId;
        try {
            this.loginLogoutLock.lock();
            this.clients.put(clientIdTracker, observer);
            clientId = clientIdTracker++;
            log.info("Client with id " + clientId + " logged in!");
        } finally {
            this.loginLogoutLock.unlock();
        }

        List<Seat> seats;
        try {
            this.readWriteLock.readLock().lock();
            seats = this.controller.findAllSeats();
        } finally {
            this.readWriteLock.readLock().unlock();
        }

        return new ResponseLogin(clientId, seats);
    }

    @Override
    public ResponseLogout logout(RequestLogout requestLogout) throws ServerException {
        try {
            this.loginLogoutLock.lock();
            this.clients.remove(requestLogout.getId());
            log.info("Client with id " + requestLogout.getId() + " logged out!");
        } finally {
            this.loginLogoutLock.unlock();
        }

        return new ResponseLogout(); // TODO does it need something?
    }

    @Override
    public ResponseReserveTicket reserveTicket(RequestReserveTicket requestReserveTicket) throws ServerException {
        Ticket ticket;
        Future<Ticket> ticketFuture;
        ticketFuture = this.executorService.submit(() -> {
            Ticket reservedTicket;
            try {
                this.readWriteLock.writeLock().lock();
                log.info("Trying to reserve a ticket with seats: {} at spectacle: {}",
                        requestReserveTicket.getSeats(),
                        requestReserveTicket.getSpectacle());
                reservedTicket = this.controller.reserveTicket(
                        requestReserveTicket.getSeats(),
                        requestReserveTicket.getSpectacle()
                );
            } finally {
                this.readWriteLock.writeLock().unlock();
            }

            return reservedTicket;
        });

        try {
            ticket = ticketFuture.get();
            this.executorService.execute(() -> {
                try {
                    this.readWriteLock.readLock().lock();
                    List<Seat> seats = this.controller.findAllSeats();
                    for (Observer observer : this.clients.values()) {
                        try {
                            observer.availableSeats(seats);
                        } catch (Exception ignored) {

                        }
                    }
                } finally {
                    this.readWriteLock.readLock().unlock();
                }
            });
        } catch (InterruptedException | ExecutionException e) {
            throw new ServerException("Couldn't reserve the ticket!", e);
        }

        return new ResponseReserveTicket(ticket);
    }

    /**
     * Notify all clients that the server is about to shut down.
     */
    public void onStop() throws ServerException {
        try {
            this.loginLogoutLock.lock();
            this.readWriteLock.writeLock().lock();
            log.info("Trying to notify all clients about server shutdown...");
            for (Observer observer : this.clients.values()) {
                try {
                    observer.onStop();
                } catch (Exception ignored) {

                }
            }

            this.executorService.shutdown();
            this.controller.saveToFile();
            log.info("Notified all clients about server shutdown!");
        } finally {
            this.readWriteLock.writeLock().unlock();
            this.loginLogoutLock.unlock();
        }
    }
}

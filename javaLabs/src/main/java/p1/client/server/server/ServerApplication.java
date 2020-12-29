package p1.client.server.server;

import p1.client.server.controller.Controller;
import p1.client.server.domain.exception.ServerException;
import p1.client.server.domain.model.Seat;
import p1.client.server.domain.model.Spectacle;
import p1.client.server.repository.SeatRepositoryImpl;
import p1.client.server.repository.SpectacleRepositoryImpl;
import p1.client.server.repository.TicketRepositoryImpl;
import p1.client.server.server.realisation.AbstractServer;
import p1.client.server.server.realisation.Server;
import p1.client.server.utils.ValidationRunnable;

import java.time.OffsetDateTime;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static p1.client.server.utils.Constants.SERVER_PORT;
import static p1.client.server.utils.Constants.SERVER_RUNNING_TIME_IN_MILLISECONDS;

public class ServerApplication {
    private static int numberOfSeats, numberOfSpectacles;
    private static float[] spectaclePrices;

    private static Controller controller;

    public static void parseArgs(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Need at least 2 arguments:" + System.lineSeparator() +
                    "> Number of seats." + System.lineSeparator() +
                    "> Number of spectacles." + System.lineSeparator() +
                    "> Spectacle prices." + System.lineSeparator());
            throw new Exception("Bad args!");
        }

        numberOfSeats = Integer.parseInt(args[0]);
        numberOfSpectacles = Integer.parseInt(args[1]);
        if (numberOfSpectacles < 1) {
            throw new Exception("The number of spectacles must be a positive integer!");
        }

        spectaclePrices = new float[numberOfSpectacles];
        for (int i = 0; i < numberOfSpectacles; i++) {
            spectaclePrices[i] = Float.parseFloat(args[2 + i]); // Offset 2
        }
    }

    private static void init() {
        SpectacleRepositoryImpl spectacleRepository = new SpectacleRepositoryImpl();
        TicketRepositoryImpl ticketRepository = new TicketRepositoryImpl();
        SeatRepositoryImpl seatRepository = new SeatRepositoryImpl();
        controller = new Controller(spectacleRepository, ticketRepository, seatRepository);

        for (int i = 0; i < numberOfSpectacles; i++) {
            Spectacle spectacle = new Spectacle(i, "Spectacle " + i, OffsetDateTime.now(), spectaclePrices[i], 0.0f);
            spectacleRepository.save(spectacle);
            for (int j = 0; j < numberOfSeats; j++) {
                Seat seat = new Seat(i * numberOfSeats + j, j, spectacle);
                seatRepository.save(seat);
            }
        }
    }

    //    ARGS: numberOfSeats numberOfSpectacles spectaclesPrices
    //    E.g.: 100 3 100 200 150
    public static void main(String[] args) throws Exception {
        org.apache.log4j.BasicConfigurator.configure();
        parseArgs(args);
        init();
        ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);

        ValidationRunnable validationRunnable = new ValidationRunnable(controller, readWriteLock, numberOfSeats, numberOfSpectacles, spectaclePrices);
        Thread validationThread = new Thread(validationRunnable);

        ServiceImpl service = new ServiceImpl(controller, readWriteLock);
        AbstractServer server = new Server(SERVER_PORT, service);

        Thread stoppingThread = new Thread(() -> {
            try {
                Thread.sleep(SERVER_RUNNING_TIME_IN_MILLISECONDS);
                service.onStop();
                validationRunnable.stop();
                server.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        try {
            validationThread.start();
            stoppingThread.start();
            server.start();
            stoppingThread.join();
            validationThread.join();
        } catch (ServerException e) {
            e.printStackTrace();
        }
    }
}

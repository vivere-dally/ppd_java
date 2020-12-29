package p1.client.server.utils.networking.service;

import lombok.extern.slf4j.Slf4j;
import p1.client.server.domain.exception.ClientWorkerException;
import p1.client.server.domain.exception.ServerException;
import p1.client.server.domain.model.Seat;
import p1.client.server.utils.networking.protocol.notification.NotificationAvailableSeats;
import p1.client.server.utils.networking.protocol.notification.NotificationOnStop;
import p1.client.server.utils.networking.protocol.request.Request;
import p1.client.server.utils.networking.protocol.request.RequestLogin;
import p1.client.server.utils.networking.protocol.request.RequestLogout;
import p1.client.server.utils.networking.protocol.request.RequestReserveTicket;
import p1.client.server.utils.networking.protocol.response.Response;
import p1.client.server.utils.networking.protocol.response.ResponseError;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import static p1.client.server.utils.Constants.CLIENT_WORKER_DELAY_BETWEEN_CHECKS_IN_MILLISECONDS;

@Slf4j
public class ClientWorker implements Runnable, Observer {
    private final Service service;
    private final Socket connection;

    private final ObjectInputStream inputStream;
    private final ObjectOutputStream outputStream;

    private volatile boolean running;

    public ClientWorker(Service service, Socket connection) {
        this.service = service;
        this.connection = connection;

        try {
            this.outputStream = new ObjectOutputStream(connection.getOutputStream());
            this.outputStream.flush();
            this.inputStream = new ObjectInputStream(connection.getInputStream());
            this.running = true;
            log.info("Constructor done!");
        } catch (IOException e) {
            throw new ClientWorkerException("Couldn't initialize input/output streams.", e);
        }
    }

    @SuppressWarnings("BusyWait")
    @Override
    public void run() {
        while (this.running) {
            try {
                Request request = (Request) this.inputStream.readObject();
                log.info("New request: " + request.toString());
                Response response = this.handleRequest(request);
                log.info("Response: " + response.toString());
                this.respond(response);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                this.stop();
            }

            try {
                Thread.sleep(CLIENT_WORKER_DELAY_BETWEEN_CHECKS_IN_MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void stop() {
        try {
            log.info("Trying to stop...");
            this.running = false;
            this.inputStream.close();
            this.outputStream.close();
            this.connection.close();
            log.info("Stopped successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Response handleRequest(Request request) {
        Response response = null;
        log.info("Handling the request...");
        try {
            if (request instanceof RequestLogin) {
                log.info("Handling the login request...");
                response = service.login((RequestLogin) request, this);
            }
            else if (request instanceof RequestLogout) {
                log.info("Handling the logout request...");
                response = service.logout((RequestLogout) request);
            }
            else if (request instanceof RequestReserveTicket) {
                log.info("Handling the reserve ticket request...");
                response = service.reserveTicket((RequestReserveTicket) request);
            }
        } catch (ServerException e) {
            response = new ResponseError(e);
        }

        return response;
    }

    private void respond(Response response) throws IOException {
        log.info("Trying to send the response {}...", response);
        this.outputStream.writeObject(response);
        this.outputStream.flush();
        log.info("Response sent!");
    }

    //region Observer

    @Override
    public void onStop() {
        NotificationOnStop notificationOnStop = new NotificationOnStop();
        try {
            this.respond(notificationOnStop);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.stop();
        }

    }

    @Override
    public void availableSeats(List<Seat> seats) {
        NotificationAvailableSeats notificationAvailableSeats = new NotificationAvailableSeats(seats);
        try {
            this.respond(notificationAvailableSeats);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //endregion
}

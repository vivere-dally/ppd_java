package p1.client.server.utils.networking.service;

import lombok.extern.slf4j.Slf4j;
import p1.client.server.domain.exception.ServerException;
import p1.client.server.domain.exception.ServiceProxyException;
import p1.client.server.utils.networking.protocol.notification.Notification;
import p1.client.server.utils.networking.protocol.notification.NotificationAvailableSeats;
import p1.client.server.utils.networking.protocol.notification.NotificationOnStop;
import p1.client.server.utils.networking.protocol.request.Request;
import p1.client.server.utils.networking.protocol.request.RequestLogin;
import p1.client.server.utils.networking.protocol.request.RequestLogout;
import p1.client.server.utils.networking.protocol.request.RequestReserveTicket;
import p1.client.server.utils.networking.protocol.response.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static p1.client.server.utils.Constants.PROXY_SERVICE_DELAY_BETWEEN_CHECKS_IN_MILLISECONDS;

@Slf4j
public class ServiceProxy implements Service {
    private final String host;
    private final int port;

    private final Queue<Response> responses;
    private final ReentrantLock reentrantLock;
    private final Condition canTake;

    private Observer observer;
    private Socket connection;

    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    private volatile boolean running;

    public ServiceProxy(String host, int port) {
        this.host = host;
        this.port = port;

        this.responses = new ArrayDeque<>();
        this.reentrantLock = new ReentrantLock(true);
        this.canTake = this.reentrantLock.newCondition();
        log.info("Constructor done!");
    }

    private void init() throws ServiceProxyException {
        try {
            this.connection = new Socket(this.host, this.port);
            this.outputStream = new ObjectOutputStream(this.connection.getOutputStream());
            this.outputStream.flush();
            this.inputStream = new ObjectInputStream(this.connection.getInputStream());
            this.running = true;
            log.info("Init success!");
            this.run();

        } catch (IOException e) {
            throw new ServiceProxyException("Could not initialize!", e);
        }
    }

    @SuppressWarnings("BusyWait")
    private void run() {
        Thread thread = new Thread(() -> {
            while (this.running) {
                try {
//  TODO Exception in thread "Thread-6" java.lang.ClassCastException: class p1.client.server.domain.model.Seat cannot be cast to class p1.client.server.utils.networking.protocol.response.Response (p1.client.server.domain.model.Seat and p1.client.server.utils.networking.protocol.response.Response are in unnamed module of loader 'app')
//	at p1.client.server.utils.networking.service.ServiceProxy.lambda$run$0(ServiceProxy.java:73)
//	at java.base/java.lang.Thread.run(Thread.java:834)


//  TODO Check for thread safety
                    Object response = this.inputStream.readObject();
                    if (response instanceof Notification) {
                        log.info("New notification: {}", response);
                        this.handleNotification((Notification) response);
                    }
                    else {

                        log.info("New response: {}", response);
                        try {
                            this.reentrantLock.lock();
                            this.responses.add((Response) response);
                            this.canTake.signal();
                        } finally {
                            this.reentrantLock.unlock();
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    this.stop();
                }

                try {
                    Thread.sleep(PROXY_SERVICE_DELAY_BETWEEN_CHECKS_IN_MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        log.info("Started the reader thread...");
    }

    private void stop() {
        this.running = false;
        log.info("ON STOP. running {}", running);
        try {
            log.info("Trying to stop...");
            this.reentrantLock.lock();
            this.observer.onStop();
            this.inputStream.close();
            this.outputStream.close();
            this.connection.close();
            this.canTake.signal();
            this.observer = null;
            log.info("Stopped successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.reentrantLock.unlock();
        }
    }

    private void sendRequest(Request request) {
        try {
            log.info("Trying to send the request: {}", request);
            this.outputStream.writeObject(request);
            this.outputStream.flush();
            log.info("Request sent!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Response readResponse() {
        Response response = null;
        try {
            this.reentrantLock.lock();
            /* There is only one consumer and one producer. No reason to use while instead of if. */
            if (this.responses.size() == 0) {
                this.canTake.await();
            }

            response = this.responses.remove();
        } catch (InterruptedException ignored) {
        } finally {
            this.reentrantLock.unlock();
        }

        return response;
    }

    private void handleNotification(Notification notification) {
        log.info("Handling notification...");
        try {
            if (notification instanceof NotificationAvailableSeats) {
                log.info("Handling occupied seats notification...");
                NotificationAvailableSeats notificationAvailableSeats = (NotificationAvailableSeats) notification;
                this.observer.availableSeats(notificationAvailableSeats.getSeats());
            }
            else if (notification instanceof NotificationOnStop) {
                log.info("Handling on stop notification...");
                this.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//region Service Implementation

    @Override
    public ResponseLogin login(RequestLogin requestLogin, Observer observer) throws ServerException {
        this.init();
        this.sendRequest(requestLogin);
        Response response = this.readResponse();
        if (response instanceof ResponseLogin) {
            this.observer = observer;
            return (ResponseLogin) response;
        }
        else if (response instanceof ResponseError) {
            ResponseError responseError = (ResponseError) response;
            throw new ServerException("Error when logging in!", responseError.getCause());
        }

        return null;
    }

    @Override
    public ResponseLogout logout(RequestLogout requestLogout) throws ServerException {
        this.sendRequest(requestLogout);
        Response response = this.readResponse();
        this.stop();
        if (response instanceof ResponseError) {
            ResponseError responseError = (ResponseError) response;
            throw new ServerException("Error when logging out!", responseError.getCause());
        }

        return (ResponseLogout) response;
    }

    @Override
    public ResponseReserveTicket reserveTicket(RequestReserveTicket requestReserveTicket) throws ServerException {
        this.sendRequest(requestReserveTicket);
        Response response = this.readResponse();
        if (response instanceof ResponseError) {
            ResponseError responseError = (ResponseError) response;
            throw new ServerException("Error when reserving a ticket!", responseError.getCause());
        }

        return (ResponseReserveTicket) response;
    }

//endregion
}

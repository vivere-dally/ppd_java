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

    private Observer observer;
    private Socket connection;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    private final Queue<Response> responses;
    private final ReentrantLock queueLock;
    private final Condition queueLockConditionCanTake;
    private volatile boolean finished;

    public ServiceProxy(String host, int port) {
        this.host = host;
        this.port = port;

        this.responses = new ArrayDeque<>();
        this.queueLock = new ReentrantLock(true);
        this.queueLockConditionCanTake = this.queueLock.newCondition();
    }

    private void init() throws ServiceProxyException {
        try {
            this.connection = new Socket(this.host, this.port);
            this.outputStream = new ObjectOutputStream(this.connection.getOutputStream());
            this.outputStream.flush();
            this.inputStream = new ObjectInputStream(this.connection.getInputStream());
            this.finished = false;
            this.startReader();
        } catch (IOException e) {
            throw new ServiceProxyException("Could not initialize!", e);
        }
    }

    private void startReader() {
        Thread thread = new Thread(new ReaderThread());
        thread.start();
    }

    private void stop() {
        this.finished = true;
        try {
            this.queueLock.lock();
            log.info("Trying to stop...");
            this.inputStream.close();
            this.outputStream.close();
            this.connection.close();
            this.observer = null;
            this.queueLockConditionCanTake.signal();
            log.info("Stopped successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.queueLock.unlock();
        }
    }

    private void sendRequest(Request request) throws ServiceProxyException {
        if (this.finished) {
            return;
        }

        try {
            log.info("Trying to send the request: {}", request);
            this.outputStream.writeObject(request);
            this.outputStream.flush();
            log.info("Request sent!");
        } catch (IOException e) {
            throw new ServiceProxyException("Error sending request: " + request, e);
        }
    }

    private Response readResponse() {
        if (this.finished) {
            return null;
        }

        Response response = null;
        try {
            this.queueLock.lock();
            /* There is only one consumer and one producer. No reason to use while instead of if. */
            if (this.responses.size() == 0 && !this.finished) {
                this.queueLockConditionCanTake.await();
            }

            response = this.responses.remove();
        } catch (InterruptedException ignored) {
        } finally {
            this.queueLock.unlock();
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
                this.observer.onStop();
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
            this.stop();
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

    private class ReaderThread implements Runnable {

        @SuppressWarnings("BusyWait")
        @Override
        public void run() {
            while (!ServiceProxy.this.finished) {
                try {
                    Object response = ServiceProxy.this.inputStream.readObject();
                    if (response instanceof Notification) {
                        log.info("New notification: {}", response);
                        ServiceProxy.this.handleNotification((Notification) response);
                    }
                    else {

                        log.info("New response: {}", response);
                        try {
                            ServiceProxy.this.queueLock.lock();
                            ServiceProxy.this.responses.add((Response) response);
                            ServiceProxy.this.queueLockConditionCanTake.signal();
                        } finally {
                            ServiceProxy.this.queueLock.unlock();
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    ServiceProxy.this.stop();
                }

                try {
                    Thread.sleep(PROXY_SERVICE_DELAY_BETWEEN_CHECKS_IN_MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

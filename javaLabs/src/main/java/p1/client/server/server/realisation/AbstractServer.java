package p1.client.server.server.realisation;


import lombok.extern.slf4j.Slf4j;
import p1.client.server.domain.exception.ServerException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public abstract class AbstractServer {
    private final int port;
    private ServerSocket server = null;
    private boolean running;

    public AbstractServer(int port) {
        this.port = port;
    }

    public void start() throws ServerException {
        try {
            log.info("Trying to create server socket...");
            running = true;
            server = new ServerSocket(port);
            log.info("Server socket created!");
            while (running) {
                log.info("Waiting for clients...");
                Socket client = server.accept();
                log.info("Client connected...");
                processRequest(client);
            }
        } catch (IOException e) {
            if (running) {
                throw new ServerException("Starting server error", e);
            }
        } finally {
            if (running) {
                stop();
            }
        }
    }

    public void stop() throws ServerException {
        log.info("Closing server...");
        try {
            running = false;
            server.close();
            log.info("Closed server successfully!");
        } catch (IOException e) {
            throw new ServerException("Closing server error", e);
        }

    }

    protected abstract void processRequest(Socket client);
}

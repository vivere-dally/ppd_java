package p1.client.server.server.realisation;

import lombok.extern.slf4j.Slf4j;
import p1.client.server.domain.exception.ServerException;

import java.net.Socket;

@Slf4j
public abstract class AbstractConcurrentServer extends AbstractServer {
    public AbstractConcurrentServer(int port) {
        super(port);
    }

    @Override
    protected void processRequest(Socket client) {
        Thread thread = this.createWorker(client);
        log.info("Trying to start client's thread...");
        thread.start();
        log.info("Client's thread started!");
    }

    @Override
    public void stop() throws ServerException {
        super.stop();
    }

    protected abstract Thread createWorker(Socket client);
}

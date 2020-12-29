package p1.client.server.server.realisation;

import lombok.extern.slf4j.Slf4j;
import p1.client.server.utils.networking.service.ClientWorker;
import p1.client.server.utils.networking.service.Service;

import java.net.Socket;

@Slf4j
public class Server extends AbstractConcurrentServer {
    private final Service service;

    public Server(int port, Service service) {
        super(port);
        this.service = service;
    }

    @Override
    protected Thread createWorker(Socket client) {
        log.info("Trying to create a new client worker...");
        ClientWorker clientWorker = new ClientWorker(service, client);
        log.info("Client worker created!");
        return new Thread(clientWorker);
    }
}

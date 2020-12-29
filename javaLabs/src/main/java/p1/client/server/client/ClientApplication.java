package p1.client.server.client;

public class ClientApplication {

    // args: numberOfClients
    public static void main(String[] args) {
        org.apache.log4j.BasicConfigurator.configure();
        Client client = new Client();
        client.run();
    }
}

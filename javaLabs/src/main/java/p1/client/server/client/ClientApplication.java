package p1.client.server.client;

public class ClientApplication {
    private static int numberOfClients = 0;

    public static void parseArgs(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Need 1 argument:" + System.lineSeparator() +
                    "> Number of clients." + System.lineSeparator());
            throw new Exception("Bad args!");
        }

        numberOfClients = Integer.parseInt(args[0]);
    }

    // args: numberOfClients
    public static void main(String[] args) throws Exception {
        org.apache.log4j.BasicConfigurator.configure();
        parseArgs(args);

        Thread[] clients = new Thread[numberOfClients];
        for (int i = 0; i < numberOfClients; i++) {
            clients[i] = new Thread(new Client());
            clients[i].start();
        }

        for (int i = 0; i < numberOfClients; i++) {
            clients[i].join();
        }

        System.out.println("done client");
    }
}

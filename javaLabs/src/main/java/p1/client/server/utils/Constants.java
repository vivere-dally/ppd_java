package p1.client.server.utils;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

public class Constants {
    public static final int NUMBER_OF_THREAD_POOL_THREADS = 10;
    public static final String HOST = "127.0.0.1";
    public static final int SERVER_PORT = 55555;//9001;
    public static final int CLIENT_MIN_PORT = 9002;
    public static final int CLIENT_MAX_PORT = 9099;
    public static final int MAX_NUMBER_OF_SEATS_PER_TICKET = 10;
    public static final int SERVER_RUNNING_TIME_IN_MILLISECONDS = 120000;
    public static final int CLIENT_REQUEST_OFFSET_IN_MILLISECONDS = 2000;
    public static final int VALIDATION_THREAD_OFFSET_IN_MILLISECONDS = 5000;
    public static final int CLIENT_WORKER_DELAY_BETWEEN_CHECKS_IN_MILLISECONDS = 1000;
    public static final int PROXY_SERVICE_DELAY_BETWEEN_CHECKS_IN_MILLISECONDS = 1000;
    public static final String VALIDATION_FILE = "validation.log";
    public static final String SPECTACLE_FILE = "spectacles.log";
    public static final String TICKETS_FILE = "tickets.log";
    public static final String SEATS_FILE = "seats.log";

    /**
     * Checks to see if a specific port is available.
     *
     * @param port the port to check for availability
     */
    public static boolean isPortAvailable(int port) {
        if (port < CLIENT_MIN_PORT || port > CLIENT_MAX_PORT) {
            throw new IllegalArgumentException("Invalid start port: " + port);
        }

        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException ignored) {
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException ignored) {
                }
            }
        }

        return false;
    }
}

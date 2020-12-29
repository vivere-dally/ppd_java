package p1.client.server.domain.exception;

public class ClientWorkerException extends RuntimeException {
    public ClientWorkerException(String message) {
        super(message);
    }

    public ClientWorkerException(String message, Throwable cause) {
        super(message, cause);
    }
}

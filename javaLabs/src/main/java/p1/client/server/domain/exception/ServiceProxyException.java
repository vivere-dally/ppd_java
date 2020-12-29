package p1.client.server.domain.exception;

public class ServiceProxyException extends RuntimeException {
    public ServiceProxyException(String message) {
        super(message);
    }

    public ServiceProxyException(String message, Throwable cause) {
        super(message, cause);
    }
}

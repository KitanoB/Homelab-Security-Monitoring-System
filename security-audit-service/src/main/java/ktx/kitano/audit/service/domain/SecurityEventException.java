package ktx.kitano.audit.service.domain;

/**
 * Exception thrown when a security event cannot be processed.
 */
public class SecurityEventException extends Exception {

    public SecurityEventException(String message) {
        super(message);
    }

    public SecurityEventException(String message, Throwable cause) {
        super(message, cause);
    }

    public SecurityEventException(Throwable cause) {
        super(cause);
    }

    public SecurityEventException() {
        super();
    }
}

package ktx.kitano.audit.service.domain;

public class SecurityEventException extends RuntimeException {

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

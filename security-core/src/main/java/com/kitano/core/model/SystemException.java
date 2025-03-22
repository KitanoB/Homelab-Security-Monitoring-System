package com.kitano.core.model;

/**
 * Exception thrown when a security event cannot be processed.
 */
public class SystemException extends Exception {

    public SystemException(String message) {
        super(message);
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public SystemException(Throwable cause) {
        super(cause);
    }

    public SystemException() {
        super();
    }
}

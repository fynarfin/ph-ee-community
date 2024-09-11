package org.mifos.connector.airtel.exception;

public class ZeebeClientStatusException extends RuntimeException {

    private final String id;

    public String getId() {
        return id;
    }

    public ZeebeClientStatusException(String message, String id, Throwable cause) {
        super(message);
        this.id = id;
    }
}

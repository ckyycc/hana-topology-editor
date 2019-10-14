package com.sap.hana.topology.exception;

public class TTException extends Exception {
    public TTException() {
        super();
    }

    public TTException(String message, Throwable cause) {
        super(message, cause);
    }

    public TTException(String message) {
        super(message);
    }

    public TTException(Throwable cause) {
        super(cause);
    }
}

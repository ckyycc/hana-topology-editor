package com.sap.hana.topology.exception;

public class TTRTException extends RuntimeException {
    public TTRTException() {
        super();
    }

    public TTRTException(String message, Throwable cause) {
        super(message, cause);
    }

    public TTRTException(String message) {
        super(message);
    }

    public TTRTException(Throwable cause) {
        super(cause);
    }
}

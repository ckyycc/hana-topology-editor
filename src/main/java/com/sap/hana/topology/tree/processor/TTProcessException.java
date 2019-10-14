package com.sap.hana.topology.tree.processor;

import com.sap.hana.topology.util.TTException;

/**
 * Topology Tree Process Exception, this exception only should be thrown by the processors
 */
public class TTProcessException extends TTException {
    public TTProcessException() {
        super();
    }

    public TTProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    public TTProcessException(String message) {
        super(message);
    }

    public TTProcessException(Throwable cause) {
        super(cause);
    }
}

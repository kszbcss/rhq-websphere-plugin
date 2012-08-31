package com.ibm.websphere.management.exception;

public class ConnectorNotAvailableException extends ConnectorException {
    public ConnectorNotAvailableException() {
        super();
    }

    public ConnectorNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectorNotAvailableException(String message) {
        super(message);
    }

    public ConnectorNotAvailableException(Throwable cause) {
        super(cause);
    }
}

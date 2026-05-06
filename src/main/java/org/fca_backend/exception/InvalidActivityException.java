package org.fca_backend.exception;

public class InvalidActivityException extends RuntimeException {
    public InvalidActivityException(String message) {
        super(message);
    }
}
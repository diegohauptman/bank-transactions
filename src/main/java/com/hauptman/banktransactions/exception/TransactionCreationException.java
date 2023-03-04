package com.hauptman.banktransactions.exception;

public class TransactionCreationException extends RuntimeException{
    public TransactionCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}

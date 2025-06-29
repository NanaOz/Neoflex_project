package com.javacode.deal.exeption;

public class CreditProcessingException extends RuntimeException {
    public CreditProcessingException(String message) {
        super(message);
    }

    public CreditProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

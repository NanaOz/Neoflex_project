package com.javacode.microservice_calculate.handler;

public class CreditCalculationException extends  RuntimeException{
    public CreditCalculationException(String message) {
        super(message);
    }

    public CreditCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.javacode.calculator.handler;

public class CreditCalculationException extends  RuntimeException{
    public CreditCalculationException(String message) {
        super(message);
    }

    public CreditCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.javacode.calculator.handler;

import com.javacode.calculator.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ScoringDataException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse handleScoringDataException(ScoringDataException e) {
        ErrorResponse response = new ErrorResponse();
        response.setCode("SCORING_DATA_ERROR");
        response.setMessage(e.getMessage());
        return response;
    }

    @ExceptionHandler(CreditCalculationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleCreditCalculationException(CreditCalculationException e) {
        ErrorResponse response = new ErrorResponse();
        response.setCode("CALCULATION_ERROR");
        response.setMessage(e.getMessage());
        return response;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(IllegalArgumentException e) {
        ErrorResponse response = new ErrorResponse();
        response.setCode("INVALID_INPUT");
        response.setMessage(e.getMessage());
        return response;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception e) {
        ErrorResponse response = new ErrorResponse();
        response.setCode("INTERNAL_ERROR");
        response.setMessage(e.getMessage());
        return response;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationExceptions(MethodArgumentNotValidException e) {
        ErrorResponse response = new ErrorResponse();
        response.setCode("VALIDATION_ERROR");
        response.setMessage(e.getMessage());
        return response;
    }
}
package com.javacode.statment.handler;

import com.javacode.statment.dto.ErrorResponse;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(StatementProcessingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleStatementProcessingException(StatementProcessingException ex) {
        return new ErrorResponse("STATEMENT_PROCESSING_ERROR", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return new ErrorResponse("VALIDATION_ERROR", "Invalid input data", errors);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAllExceptions(Exception ex) {
        return new ErrorResponse("INTERNAL_ERROR", "Internal server error");
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleStatementNotFoundException(ValidationException ex) {
        return new ErrorResponse(
                "VALIDATION_ERROR",
                ex.getMessage(),
                Collections.emptyList()
        );
    }

    @ExceptionHandler(PreScoringException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handlePreScoringException(PreScoringException ex) {
        return new ErrorResponse(
                "PRESCORING_ERROR",
                ex.getMessage(),
                Collections.emptyList()
        );
    }
}

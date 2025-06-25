package com.javacode.calculator.handler;

import com.javacode.calculator.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ScoringDataException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse handleScoringDataException(ScoringDataException e) {
        ErrorResponse response = new ErrorResponse();
        response.setCode("SCORING_DATA_ERROR");
        response.setMessage("Ошибка при обработке данных клиента");
        response.setDetails(List.of(e.getMessage()));
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
        response.setMessage("Произошла внутренняя ошибка сервера");
        return response;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationExceptions(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        ErrorResponse response = new ErrorResponse();
        response.setCode("VALIDATION_ERROR");
        response.setMessage("Неверные входные данные");
        response.setDetails(errors);
        return response;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        ErrorResponse response = new ErrorResponse();
        response.setCode("INVALID_JSON");
        response.setMessage("Неверный формат JSON");
        return response;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingParams(MissingServletRequestParameterException e) {
        ErrorResponse response = new ErrorResponse();
        response.setCode("MISSING_PARAM");
        response.setMessage("Отсутствует обязательный параметр: " + e.getParameterName());
        return response;
    }
}
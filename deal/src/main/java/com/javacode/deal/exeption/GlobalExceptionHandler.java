package com.javacode.deal.exeption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CalculatorServiceException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleCalculatorServiceException(CalculatorServiceException ex) {
        return new ErrorResponse("Calculator Service недоступен", ex.getMessage());
    }

    @ExceptionHandler(CreditRequestFailedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleCreditRequestFailedException(CreditRequestFailedException ex) {
        return new ErrorResponse("Не удалось выполнить запрос на получение кредита", ex.getMessage());
    }

    @ExceptionHandler(StatementNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleStatementNotFoundException(StatementNotFoundException ex) {
        return new ErrorResponse("Заявление не найдено", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAllExceptions(Exception ex) {
        return new ErrorResponse("Внутренняя ошибка сервера", "непредвиденная ошибка");
    }

    @ExceptionHandler(CreditProcessingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleCreditProcessingException(CreditProcessingException ex) {
        return new ErrorResponse("Ошибка при обработке кредита", ex.getMessage());
    }
}

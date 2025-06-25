package com.javacode.calculator.controller;

import com.javacode.calculator.dto.Credit;
import com.javacode.calculator.dto.LoanOffer;
import com.javacode.calculator.dto.LoanStatementRequest;
import com.javacode.calculator.dto.ScoringData;
import com.javacode.calculator.service.CalculatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CalculatorController implements CalculatorApi {
    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @Override
    public ResponseEntity<List<LoanOffer>> calculateOffers(LoanStatementRequest requestDto) {
        return calculatorService.calculateOffers(requestDto);
    }

    @Override
    public ResponseEntity<Credit> calculateCredit(ScoringData scoringDataDto) {
        return calculatorService.calculateCredit(scoringDataDto);
    }
}

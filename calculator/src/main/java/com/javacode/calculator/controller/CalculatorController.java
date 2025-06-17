package com.javacode.calculator.controller;

import com.javacode.calculator.dto.CreditDto;
import com.javacode.calculator.dto.LoanOfferDto;
import com.javacode.calculator.dto.LoanStatementRequestDto;
import com.javacode.calculator.dto.ScoringDataDto;
import com.javacode.calculator.service.CalculatorService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/calculator")
public class CalculatorController {
    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @PostMapping("/offers")
    public List<LoanOfferDto> calculateOffers(@RequestBody LoanStatementRequestDto request) {
        return calculatorService.calculateOffers(request);
    }

    @PostMapping("/calc")
    public CreditDto calculateCredit(@RequestBody ScoringDataDto scoringDataDto) {
        return calculatorService.calculateCredit(scoringDataDto);
    }
}

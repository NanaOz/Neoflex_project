package com.javacode.calculator.controller;

import com.javacode.calculator.dto.CreditDto;
import com.javacode.calculator.dto.LoanOfferDto;
import com.javacode.calculator.dto.LoanStatementRequestDto;
import com.javacode.calculator.dto.ScoringDataDto;
import com.javacode.calculator.service.CalculatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/calculator")
@Tag(name = "Calculator API", description = "Микросервис для расчета кредитных предложений и условий")
public class CalculatorController {
    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @Operation(
            summary = "Рассчитать кредитные предложения",
            description = "Возвращает список возможных кредитных предложений на основе заявки"
    )
    @PostMapping("/offers")
    public List<LoanOfferDto> calculateOffers(@RequestBody LoanStatementRequestDto request) {
        return calculatorService.calculateOffers(request);
    }

    @Operation(
            summary = "Рассчитать условия кредита",
            description = "Возвращает детализированные условия кредита после скоринга"
    )
    @PostMapping("/calc")
    public CreditDto calculateCredit(@RequestBody ScoringDataDto scoringDataDto) {
        return calculatorService.calculateCredit(scoringDataDto);
    }
}

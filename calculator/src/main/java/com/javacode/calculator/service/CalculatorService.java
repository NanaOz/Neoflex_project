package com.javacode.calculator.service;

import com.javacode.calculator.dto.CreditDto;
import com.javacode.calculator.dto.LoanOfferDto;
import com.javacode.calculator.dto.LoanStatementRequestDto;
import com.javacode.calculator.dto.ScoringDataDto;

import java.util.List;

public interface CalculatorService {
    List<LoanOfferDto> calculateOffers(LoanStatementRequestDto requestDto);
    CreditDto calculateCredit(ScoringDataDto scoringDataDto);
}

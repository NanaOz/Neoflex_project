package com.javacode.microservice_calculate.service;

import com.javacode.microservice_calculate.dto.CreditDto;
import com.javacode.microservice_calculate.dto.LoanOfferDto;
import com.javacode.microservice_calculate.dto.LoanStatementRequestDto;
import com.javacode.microservice_calculate.dto.ScoringDataDto;

import java.util.List;

public interface CalculatorService {
    List<LoanOfferDto> calculateOffers(LoanStatementRequestDto requestDto);
    CreditDto calculateCredit(ScoringDataDto scoringDataDto);
}

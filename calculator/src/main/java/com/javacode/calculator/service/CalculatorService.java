package com.javacode.calculator.service;

import com.javacode.calculator.dto.Credit;
import com.javacode.calculator.dto.LoanOffer;
import com.javacode.calculator.dto.LoanStatementRequest;
import com.javacode.calculator.dto.ScoringData;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;

import javax.annotation.processing.Generated;
import java.util.List;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen")
@Validated
public interface CalculatorService {
    ResponseEntity<List<LoanOffer>> calculateOffers(LoanStatementRequest requestDto);
    ResponseEntity<Credit> calculateCredit(ScoringData scoringDataDto);
}

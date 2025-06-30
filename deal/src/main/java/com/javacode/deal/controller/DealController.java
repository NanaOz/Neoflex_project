package com.javacode.deal.controller;

import com.javacode.deal.dto.FinishRegistrationRequestDto;
import com.javacode.deal.dto.LoanOfferDto;
import com.javacode.deal.dto.LoanStatementRequestDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

public interface DealController {
    @PostMapping("/statement")
    ResponseEntity<List<LoanOfferDto>> createStatement(@Valid @RequestBody LoanStatementRequestDto request);

    @PostMapping("/offer/select")
    ResponseEntity<Void> selectOffer(@Valid @RequestBody LoanOfferDto offer);

    @PostMapping("/calculate/{statementId}")
    ResponseEntity<Void> calculateCredit(
            @PathVariable UUID statementId,
            @Valid @RequestBody FinishRegistrationRequestDto request);
}

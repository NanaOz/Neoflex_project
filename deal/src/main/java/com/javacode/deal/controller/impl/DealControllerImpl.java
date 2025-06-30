package com.javacode.deal.controller.impl;

import com.javacode.deal.controller.DealController;
import com.javacode.deal.dto.FinishRegistrationRequestDto;
import com.javacode.deal.dto.LoanOfferDto;
import com.javacode.deal.dto.LoanStatementRequestDto;
import com.javacode.deal.service.CreditService;
import com.javacode.deal.service.OfferService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/deal")
public class DealControllerImpl implements DealController {
    private final OfferService offerService;
    private final CreditService creditService;

    @Override
    @Operation(summary = "Расчёт возможных условий кредита")
    public ResponseEntity<List<LoanOfferDto>> createStatement(@RequestBody LoanStatementRequestDto request) {
        List<LoanOfferDto> offers = offerService.getLoanOfferList(request);
        return ResponseEntity.ok(offers);
    }

    @Override
    @Operation(summary = "Выбор одного из предложений")
    public ResponseEntity<Void> selectOffer(@RequestBody LoanOfferDto offer) {
        offerService.applyOffer(offer);
        return ResponseEntity.ok().build();
    }

    @Override
    @Operation(summary = "Завершение регистрации + полный подсчёт кредита")
    public ResponseEntity<Void> calculateCredit(
            @PathVariable UUID statementId,
            @RequestBody FinishRegistrationRequestDto request) {
        creditService.finishCreditRegistration(statementId, request);
        return ResponseEntity.ok().build();
    }
}

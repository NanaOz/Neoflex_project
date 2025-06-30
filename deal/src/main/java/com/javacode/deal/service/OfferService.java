package com.javacode.deal.service;

import com.javacode.deal.dto.LoanOfferDto;
import com.javacode.deal.dto.LoanStatementRequestDto;

import java.util.List;

public interface OfferService {
    List<LoanOfferDto> getLoanOfferList(LoanStatementRequestDto loanStatementRequestDto);

    void applyOffer(LoanOfferDto loanOfferDto);
}

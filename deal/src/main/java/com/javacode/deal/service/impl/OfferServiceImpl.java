package com.javacode.deal.service.impl;

import com.javacode.deal.dto.LoanOfferDto;
import com.javacode.deal.dto.LoanStatementRequestDto;
import com.javacode.deal.model.entity.Client;
import com.javacode.deal.model.entity.Statement;
import com.javacode.deal.service.CalculatorService;
import com.javacode.deal.service.ClientService;
import com.javacode.deal.service.OfferService;
import com.javacode.deal.service.StatementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OfferServiceImpl implements OfferService {
    private final ClientService clientService;
    private final StatementService statementService;
    private final CalculatorService calculatorService;

    public OfferServiceImpl(ClientService clientService, StatementService statementService, CalculatorService calculatorService) {
        this.clientService = clientService;
        this.statementService = statementService;
        this.calculatorService = calculatorService;
    }

    @Override
    @Transactional
    public List<LoanOfferDto> getLoanOfferList(LoanStatementRequestDto loanStatementRequestDto) {

        Client client = clientService.createClient(loanStatementRequestDto);
        Statement statement = statementService.createStatement(client);

        List<LoanOfferDto> offers = calculatorService.getOffers(loanStatementRequestDto);
        offers.forEach(offer -> offer.setStatementId(statement.getStatementId()));

        return offers.stream()
                .sorted(Comparator.comparing(LoanOfferDto::getRate).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public void applyOffer(LoanOfferDto loanOfferDto) {
        statementService.applyOfferToStatement(
                loanOfferDto.getStatementId(),
                loanOfferDto
        );
    }
}

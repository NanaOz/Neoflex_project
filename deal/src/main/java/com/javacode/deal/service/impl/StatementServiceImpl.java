package com.javacode.deal.service.impl;


import com.javacode.deal.dto.CreditDto;
import com.javacode.deal.dto.LoanOfferDto;
import com.javacode.deal.model.entity.Client;
import com.javacode.deal.model.entity.Statement;
import com.javacode.deal.model.enums.ApplicationStatus;
import com.javacode.deal.model.enums.ChangeType;
import com.javacode.deal.model.jsonb.LoanOffer;
import com.javacode.deal.model.jsonb.StatusHistory;
import com.javacode.deal.repository.StatementRepository;
import com.javacode.deal.service.StatementService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class StatementServiceImpl implements StatementService {

    private final StatementRepository statementRepository;

    public StatementServiceImpl(StatementRepository statementRepository) {
        this.statementRepository = statementRepository;
    }

    @Override
    @Transactional
    public Statement createStatement(Client client) {

        Statement statement = Statement.builder()
                .statementId(UUID.randomUUID())
                .client(client)
                .status(ApplicationStatus.PREAPPROVAL)
                .creationDate(new Timestamp(System.currentTimeMillis()))
                .statusHistory(new ArrayList<>())
                .build();

        StatusHistory initialHistory = StatusHistory.builder()
                .status(ApplicationStatus.PREAPPROVAL)
                .time(new Timestamp(System.currentTimeMillis()))
                .changeType(ChangeType.AUTOMATIC)
                .build();

        statement.addStatusHistory(initialHistory);

        statement = statementRepository.save(statement);
        return statement;
    }

    @Override
    public Statement getStatement(UUID statementId) {
        return statementRepository.findById(statementId)
                .orElseThrow(() -> new EntityNotFoundException("Statement not found with ID: " + statementId));
    }

    @Override
    @Transactional
    public Statement updateStatementStatus(UUID statementId, ApplicationStatus status, ChangeType changeType) {

        Statement statement = getStatement(statementId);
        statement.setStatus(status);

        StatusHistory history = StatusHistory.builder()
                .status(status)
                .time(new Timestamp(System.currentTimeMillis()))
                .changeType(changeType)
                .build();

        statement.addStatusHistory(history);

        statement = statementRepository.save(statement);
        return statement;
    }

    @Override
    @Transactional
    public Statement applyOfferToStatement(UUID statementId, LoanOfferDto offer) {

        Statement statement = getStatement(statementId);

        LoanOffer loanOffer = LoanOffer.builder()
                .statementId(offer.getStatementId())
                .requestedAmount(offer.getRequestedAmount())
                .totalAmount(offer.getTotalAmount())
                .term(offer.getTerm())
                .monthlyPayment(offer.getMonthlyPayment())
                .rate(offer.getRate())
                .isInsuranceEnabled(offer.getIsInsuranceEnabled())
                .isSalaryClient(offer.getIsSalaryClient())
                .build();

        statement.setAppliedOffer(loanOffer);
        statement = updateStatementStatus(statementId, ApplicationStatus.APPROVED, ChangeType.MANUAL);

        return statement;
    }

    @Override
    @Transactional
    public Statement addCreditToStatement(UUID statementId, CreditDto creditDto) {

        Statement statement = getStatement(statementId);
        statement = updateStatementStatus(statementId, ApplicationStatus.CC_APPROVED, ChangeType.AUTOMATIC);

        return statement;
    }
}

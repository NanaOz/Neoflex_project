package com.javacode.deal.service;

import com.javacode.deal.dto.CreditDto;
import com.javacode.deal.dto.LoanOfferDto;
import com.javacode.deal.model.entity.Client;
import com.javacode.deal.model.entity.Statement;
import com.javacode.deal.model.enums.ApplicationStatus;
import com.javacode.deal.model.enums.ChangeType;

import java.util.UUID;

public interface StatementService {
    Statement createStatement(Client client);

    Statement getStatement(UUID statementId);

    Statement updateStatementStatus(UUID statementId, ApplicationStatus status, ChangeType changeType);

    Statement applyOfferToStatement(UUID statementId, LoanOfferDto offer);

    Statement addCreditToStatement(UUID statementId, CreditDto creditDto);
}

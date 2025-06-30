package com.javacode.deal.service;

import com.javacode.deal.dto.FinishRegistrationRequestDto;
import com.javacode.deal.dto.LoanStatementRequestDto;
import com.javacode.deal.model.entity.Client;

import java.util.UUID;

public interface ClientService {
    Client createClient(LoanStatementRequestDto request);

    Client updateClient(UUID clientId, FinishRegistrationRequestDto request);
}

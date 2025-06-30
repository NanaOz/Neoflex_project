package com.javacode.deal.service;

import com.javacode.deal.dto.FinishRegistrationRequestDto;

import java.util.UUID;

public interface CreditService {
    void finishCreditRegistration(UUID statementId, FinishRegistrationRequestDto frrDto);

}

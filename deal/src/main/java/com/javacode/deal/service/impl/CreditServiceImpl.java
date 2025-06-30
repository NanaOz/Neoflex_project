package com.javacode.deal.service.impl;

import com.javacode.deal.dto.CreditDto;
import com.javacode.deal.dto.FinishRegistrationRequestDto;
import com.javacode.deal.dto.ScoringDataDto;
import com.javacode.deal.dto.dtoMapper.CreditMapper;
import com.javacode.deal.exeption.CreditProcessingException;
import com.javacode.deal.exeption.CreditRequestFailedException;
import com.javacode.deal.model.entity.Client;
import com.javacode.deal.model.entity.Credit;
import com.javacode.deal.model.entity.Statement;
import com.javacode.deal.model.enums.ApplicationStatus;
import com.javacode.deal.model.enums.ChangeType;
import com.javacode.deal.model.enums.CreditStatus;
import com.javacode.deal.model.jsonb.LoanOffer;
import com.javacode.deal.repository.CreditRepository;
import com.javacode.deal.service.CalculatorService;
import com.javacode.deal.service.ClientService;
import com.javacode.deal.service.CreditService;
import com.javacode.deal.service.StatementService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CreditServiceImpl implements CreditService {
    private final ClientService clientService;
    private final StatementService statementService;
    private final CreditRepository creditRepository;
    private final CalculatorService calculatorService;
    private final CreditMapper creditMapper;

    public CreditServiceImpl(ClientService clientService, StatementService statementService, CreditRepository creditRepository, CalculatorService calculatorService, CreditMapper creditMapper) {
        this.clientService = clientService;
        this.statementService = statementService;
        this.creditRepository = creditRepository;
        this.calculatorService = calculatorService;
        this.creditMapper = creditMapper;
    }

    @Override
    @Transactional
    public void finishCreditRegistration(UUID statementId, FinishRegistrationRequestDto frrDto) {
        try {
            Statement statement = statementService.getStatement(statementId);
            Client client = statement.getClient();

            // Обновляем данные клиента
            client = clientService.updateClient(client.getClientId(), frrDto);

            // Собираем ScoringDataDto
            ScoringDataDto scoringData = buildScoringDataDto(client, frrDto, statement);

            // Получаем расчет кредита от калькулятора
            CreditDto creditDto;
            try {
                creditDto = calculatorService.getCredit(scoringData);
            } catch (CreditRequestFailedException e) {
                // Обновляем статус заявки на "отклонено"
                statementService.updateStatementStatus(
                        statementId,
                        ApplicationStatus.CC_DENIED,
                        ChangeType.AUTOMATIC
                );
                throw new CreditProcessingException("Не удалось рассчитать кредит: " + e.getMessage());
            }

            // Сохраняем кредит
            Credit credit = creditMapper.toEntity(creditDto);
            credit.setCreditStatus(CreditStatus.CALCULATED);
            credit = creditRepository.save(credit);

            // Обновляем заявку
            statementService.addCreditToStatement(statementId, creditDto);

        } catch (EntityNotFoundException e) {
            throw new CreditProcessingException("Заявление или клиент не найден: " + e.getMessage());
        }
    }

    private ScoringDataDto buildScoringDataDto(Client client, FinishRegistrationRequestDto frrDto, Statement statement) {
        LoanOffer appliedOffer = statement.getAppliedOffer();

        return ScoringDataDto.builder()
                .amount(appliedOffer.getRequestedAmount())
                .term(appliedOffer.getTerm())
                .firstName(client.getFirstName())
                .lastName(client.getLastName())
                .middleName(client.getMiddleName())
                .gender(client.getGender())
                .birthdate(client.getBirthDate())
                .passportSeries(client.getPassport().getSeries())
                .passportNumber(client.getPassport().getNumber())
                .passportIssueDate(frrDto.getPassportIssueDate())
                .passportIssueBranch(frrDto.getPassportIssueBranch())
                .maritalStatus(client.getMaritalStatus())
                .dependentAmount(client.getDependentAmount())
                .employment(frrDto.getEmployment())
                .accountNumber(client.getAccountNumber())
                .isInsuranceEnabled(appliedOffer.getIsInsuranceEnabled())
                .isSalaryClient(appliedOffer.getIsSalaryClient())
                .build();
    }
}

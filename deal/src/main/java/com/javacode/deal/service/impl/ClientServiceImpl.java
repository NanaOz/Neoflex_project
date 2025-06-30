package com.javacode.deal.service.impl;

import com.javacode.deal.dto.FinishRegistrationRequestDto;
import com.javacode.deal.dto.LoanStatementRequestDto;
import com.javacode.deal.model.entity.Client;
import com.javacode.deal.model.jsonb.Employment;
import com.javacode.deal.model.jsonb.Passport;
import com.javacode.deal.repository.ClientRepository;
import com.javacode.deal.service.ClientService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    public ClientServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    @Transactional
    public Client createClient(LoanStatementRequestDto request) {

        Passport passport = Passport.builder()
                .series(request.getPassportSeries())
                .number(request.getPassportNumber())
                .build();

        Client client = Client.builder()
                .clientId(UUID.randomUUID())
                .lastName(request.getLastName())
                .firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .birthDate(request.getBirthdate())
                .email(request.getEmail())
                .passport(passport)
                .build();

        client = clientRepository.save(client);
        return client;
    }

    @Override
    @Transactional
    public Client updateClient(UUID clientId, FinishRegistrationRequestDto request) {

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Не найден клиент с ID: " + clientId));

        Employment employment = Employment.builder()
                .status(request.getEmployment().getEmploymentStatus())
                .employerInn(request.getEmployment().getEmployerINN())
                .salary(request.getEmployment().getSalary())
                .position(request.getEmployment().getEmploymentPosition())
                .workExperienceTotal(request.getEmployment().getWorkExperienceTotal())
                .workExperienceCurrent(request.getEmployment().getWorkExperienceCurrent())
                .build();

        Passport passport = client.getPassport();
        passport.setIssueBranch(request.getPassportIssueBranch());
        passport.setIssueDate(request.getPassportIssueDate());

        client.setGender(request.getGender())
                .setMaritalStatus(request.getMaritalStatus())
                .setDependentAmount(request.getDependentAmount())
                .setPassport(passport)
                .setEmployment(employment)
                .setAccountNumber(request.getAccountNumber());

        client = clientRepository.save(client);
        return client;
    }
}

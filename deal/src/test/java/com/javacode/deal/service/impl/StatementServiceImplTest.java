package com.javacode.deal.service.impl;

import com.javacode.deal.dto.CreditDto;
import com.javacode.deal.dto.LoanOfferDto;
import com.javacode.deal.model.entity.Client;
import com.javacode.deal.model.entity.Statement;
import com.javacode.deal.model.enums.ApplicationStatus;
import com.javacode.deal.model.enums.ChangeType;
import com.javacode.deal.model.jsonb.StatusHistory;
import com.javacode.deal.repository.StatementRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StatementServiceImplTest {
    @Mock
    private StatementRepository statementRepository;

    @InjectMocks
    private StatementServiceImpl statementService;

    @Test
    void createStatement_ShouldCreateNewStatementWithInitialStatus() {
        // Arrange
        Client client = new Client();
        client.setClientId(UUID.randomUUID());

        when(statementRepository.save(any(Statement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Statement result = statementService.createStatement(client);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getStatementId());
        assertEquals(client, result.getClient());
        assertEquals(ApplicationStatus.PREAPPROVAL, result.getStatus());
        assertNotNull(result.getCreationDate());
        assertEquals(1, result.getStatusHistory().size());

        StatusHistory history = result.getStatusHistory().get(0);
        assertEquals(ApplicationStatus.PREAPPROVAL, history.getStatus());
        assertEquals(ChangeType.AUTOMATIC, history.getChangeType());
        assertNotNull(history.getTime());

        verify(statementRepository).save(any(Statement.class));
    }

    @Test
    void getStatement_ShouldReturnStatementWhenExists() {
        // Arrange
        UUID statementId = UUID.randomUUID();
        Statement expectedStatement = new Statement();
        expectedStatement.setStatementId(statementId);

        when(statementRepository.findById(statementId)).thenReturn(Optional.of(expectedStatement));

        // Act
        Statement result = statementService.getStatement(statementId);

        // Assert
        assertNotNull(result);
        assertEquals(statementId, result.getStatementId());
        verify(statementRepository).findById(statementId);
    }

    @Test
    void getStatement_ShouldThrowExceptionWhenNotFound() {
        // Arrange
        UUID statementId = UUID.randomUUID();
        when(statementRepository.findById(statementId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> statementService.getStatement(statementId));
        verify(statementRepository).findById(statementId);
    }

    @Test
    void updateStatementStatus_ShouldUpdateStatusAndAddHistory() {
        // Arrange
        UUID statementId = UUID.randomUUID();
        Statement existingStatement = new Statement();
        existingStatement.setStatementId(statementId);
        existingStatement.setStatus(ApplicationStatus.PREAPPROVAL);
        existingStatement.setStatusHistory(new ArrayList<>());

        when(statementRepository.findById(statementId)).thenReturn(Optional.of(existingStatement));
        when(statementRepository.save(any(Statement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Statement result = statementService.updateStatementStatus(
                statementId, ApplicationStatus.APPROVED, ChangeType.MANUAL);

        // Assert
        assertNotNull(result);
        assertEquals(ApplicationStatus.APPROVED, result.getStatus());
        assertEquals(1, result.getStatusHistory().size());

        StatusHistory history = result.getStatusHistory().get(0);
        assertEquals(ApplicationStatus.APPROVED, history.getStatus());
        assertEquals(ChangeType.MANUAL, history.getChangeType());
        assertNotNull(history.getTime());

        verify(statementRepository).findById(statementId);
        verify(statementRepository).save(existingStatement);
    }

    @Test
    void applyOfferToStatement_ShouldSetOfferAndUpdateStatus() {
        // Arrange
        UUID statementId = UUID.randomUUID();
        Statement existingStatement = new Statement();
        existingStatement.setStatementId(statementId);

        LoanOfferDto offerDto = new LoanOfferDto();
        offerDto.setStatementId(statementId);

        when(statementRepository.findById(statementId)).thenReturn(Optional.of(existingStatement));
        when(statementRepository.save(existingStatement)).thenReturn(existingStatement);

        // Act
        Statement result = statementService.applyOfferToStatement(statementId, offerDto);

        // Assert
        assertNotNull(result.getAppliedOffer());
        assertEquals(ApplicationStatus.APPROVED, result.getStatus());

        verify(statementRepository, times(2)).findById(statementId);
        verify(statementRepository, times(1)).save(existingStatement);
    }

    @Test
    void addCreditToStatement_ShouldUpdateStatus() {
        // Arrange
        UUID statementId = UUID.randomUUID();
        Statement existingStatement = new Statement();
        existingStatement.setStatementId(statementId);

        when(statementRepository.findById(statementId)).thenReturn(Optional.of(existingStatement));
        when(statementRepository.save(existingStatement)).thenReturn(existingStatement);

        // Act
        Statement result = statementService.addCreditToStatement(statementId, new CreditDto());

        // Assert
        assertEquals(ApplicationStatus.CC_APPROVED, result.getStatus());

        verify(statementRepository, times(2)).findById(statementId);
        verify(statementRepository, times(1)).save(existingStatement);
    }
}

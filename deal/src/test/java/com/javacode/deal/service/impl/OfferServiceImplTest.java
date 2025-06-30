package com.javacode.deal.service.impl;

import com.javacode.deal.dto.LoanOfferDto;
import com.javacode.deal.dto.LoanStatementRequestDto;
import com.javacode.deal.model.entity.Client;
import com.javacode.deal.model.entity.Statement;
import com.javacode.deal.service.CalculatorService;
import com.javacode.deal.service.ClientService;
import com.javacode.deal.service.StatementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OfferServiceImplTest {
    @Mock
    private ClientService clientService;

    @Mock
    private StatementService statementService;

    @Mock
    private CalculatorService calculatorService;

    @InjectMocks
    private OfferServiceImpl offerService;

    @Test
    void getLoanOfferList_ShouldReturnSortedOffers() {
        // Arrange
        LoanStatementRequestDto requestDto = new LoanStatementRequestDto();
        Client client = new Client();
        client.setClientId(UUID.randomUUID());

        Statement statement = new Statement();
        statement.setStatementId(UUID.randomUUID());

        LoanOfferDto offer1 = new LoanOfferDto();
        offer1.setRate(BigDecimal.valueOf(10.5));

        LoanOfferDto offer2 = new LoanOfferDto();
        offer2.setRate(BigDecimal.valueOf(8.5));

        LoanOfferDto offer3 = new LoanOfferDto();
        offer3.setRate(BigDecimal.valueOf(12.0));

        when(clientService.createClient(requestDto)).thenReturn(client);
        when(statementService.createStatement(client)).thenReturn(statement);
        when(calculatorService.getOffers(requestDto))
                .thenReturn(List.of(offer1, offer2, offer3));

        // Act
        List<LoanOfferDto> result = offerService.getLoanOfferList(requestDto);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(BigDecimal.valueOf(12.0), result.get(0).getRate());
        assertEquals(BigDecimal.valueOf(10.5), result.get(1).getRate());
        assertEquals(BigDecimal.valueOf(8.5), result.get(2).getRate());

        // Verify all offers have statementId set
        assertTrue(result.stream().allMatch(o -> o.getStatementId().equals(statement.getStatementId())));

        verify(clientService).createClient(requestDto);
        verify(statementService).createStatement(client);
        verify(calculatorService).getOffers(requestDto);
    }

    @Test
    void getLoanOfferList_WithEmptyOffers_ShouldReturnEmptyList() {
        // Arrange
        LoanStatementRequestDto requestDto = new LoanStatementRequestDto();
        Client client = new Client();
        Statement statement = new Statement();

        when(clientService.createClient(requestDto)).thenReturn(client);
        when(statementService.createStatement(client)).thenReturn(statement);
        when(calculatorService.getOffers(requestDto)).thenReturn(List.of());

        // Act
        List<LoanOfferDto> result = offerService.getLoanOfferList(requestDto);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void applyOffer_ShouldDelegateToStatementService() {
        // Arrange
        UUID statementId = UUID.randomUUID();
        LoanOfferDto offerDto = new LoanOfferDto();
        offerDto.setStatementId(statementId);

        // Act
        offerService.applyOffer(offerDto);

        // Assert
        verify(statementService).applyOfferToStatement(statementId, offerDto);
        verifyNoMoreInteractions(statementService);
        verifyNoInteractions(clientService, calculatorService);
    }

    @Test
    void applyOffer_ShouldCallStatementServiceWithCorrectParameters() {
        // Arrange
        UUID statementId = UUID.randomUUID();
        LoanOfferDto offerDto = new LoanOfferDto();
        offerDto.setStatementId(statementId);

        // Act
        offerService.applyOffer(offerDto);

        // Assert
        verify(statementService, times(1))
                .applyOfferToStatement(statementId, offerDto);
        verifyNoMoreInteractions(statementService);
    }
}

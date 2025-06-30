package com.javacode.deal.service.impl;

import com.javacode.deal.dto.CreditDto;
import com.javacode.deal.dto.EmploymentDto;
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
import com.javacode.deal.model.enums.EmploymentPosition;
import com.javacode.deal.model.enums.EmploymentStatus;
import com.javacode.deal.model.enums.Gender;
import com.javacode.deal.model.enums.MaritalStatus;
import com.javacode.deal.model.jsonb.LoanOffer;
import com.javacode.deal.model.jsonb.Passport;
import com.javacode.deal.repository.CreditRepository;
import com.javacode.deal.service.CalculatorService;
import com.javacode.deal.service.ClientService;
import com.javacode.deal.service.StatementService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreditServiceImplTest {
    @Mock
    private ClientService clientService;

    @Mock
    private StatementService statementService;

    @Mock
    private CreditRepository creditRepository;

    @Mock
    private CalculatorService calculatorService;

    @Mock
    private CreditMapper creditMapper;

    @InjectMocks
    private CreditServiceImpl creditService;

    private LoanOffer createTestLoanOffer() throws Exception {
        Constructor<LoanOffer> constructor = LoanOffer.class.getDeclaredConstructor(
                UUID.class, BigDecimal.class, BigDecimal.class, Integer.class,
                BigDecimal.class, BigDecimal.class, Boolean.class, Boolean.class);
        constructor.setAccessible(true);
        return constructor.newInstance(
                UUID.randomUUID(), BigDecimal.valueOf(100000), BigDecimal.valueOf(95000),
                12, BigDecimal.valueOf(8.5), BigDecimal.valueOf(15000), true, false);
    }

    private Statement createTestStatement(UUID statementId, Client client) throws Exception {
        Statement statement = new Statement();
        statement.setStatementId(statementId);
        statement.setClient(client);
        statement.setAppliedOffer(createTestLoanOffer());
        return statement;
    }

    private Client createTestClient(UUID clientId) {
        return Client.builder()
                .clientId(clientId)
                .firstName("Ivan")
                .lastName("Ivanov")
                .middleName("Ivanovich")
                .birthDate(LocalDate.of(1990, 1, 1))
                .passport(Passport.builder()
                        .series("1234")
                        .number("567890")
                        .build())
                .build();
    }

    private FinishRegistrationRequestDto createTestFinishRegistrationRequest() throws Exception {
        FinishRegistrationRequestDto frrDto = new FinishRegistrationRequestDto();
        frrDto.setPassportIssueDate(LocalDate.now());
        frrDto.setPassportIssueBranch("УФМС РФ");
        frrDto.setAccountNumber("1234567890");

        EmploymentDto employmentDto = new EmploymentDto();
        employmentDto.setEmploymentStatus(EmploymentStatus.EMPLOYED);
        employmentDto.setEmployerINN("123456789012");
        employmentDto.setSalary(BigDecimal.valueOf(50000));
        employmentDto.setEmploymentPosition(EmploymentPosition.MID_MANAGER);
        employmentDto.setWorkExperienceTotal(5);
        employmentDto.setWorkExperienceCurrent(2);

        frrDto.setEmployment(employmentDto);
        return frrDto;
    }

    @Test
    void finishCreditRegistration_Success() throws Exception {
        // Arrange
        UUID statementId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        Client client = createTestClient(clientId);
        Statement statement = createTestStatement(statementId, client);
        FinishRegistrationRequestDto frrDto = createTestFinishRegistrationRequest();

        CreditDto creditDto = new CreditDto();
        creditDto.setAmount(BigDecimal.valueOf(100000));
        creditDto.setTerm(12);

        Credit credit = new Credit();
        credit.setCreditId(UUID.randomUUID());
        credit.setCreditStatus(CreditStatus.CALCULATED);

        when(statementService.getStatement(statementId)).thenReturn(statement);
        when(clientService.updateClient(clientId, frrDto)).thenReturn(client);
        when(calculatorService.getCredit(any(ScoringDataDto.class))).thenReturn(creditDto);
        when(creditMapper.toEntity(creditDto)).thenReturn(credit);
        when(creditRepository.save(credit)).thenReturn(credit);

        // Act
        creditService.finishCreditRegistration(statementId, frrDto);

        // Assert
        verify(statementService).getStatement(statementId);
        verify(clientService).updateClient(clientId, frrDto);
        verify(calculatorService).getCredit(any(ScoringDataDto.class));
        verify(creditMapper).toEntity(creditDto);
        verify(creditRepository).save(credit);
        verify(statementService).addCreditToStatement(statementId, creditDto);
    }

    @Test
    void finishCreditRegistration_WhenCalculatorFails_ShouldDenyApplication() throws Exception {
        // Arrange
        UUID statementId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        Client client = createTestClient(clientId);
        Statement statement = createTestStatement(statementId, client);
        FinishRegistrationRequestDto frrDto = createTestFinishRegistrationRequest();

        when(statementService.getStatement(statementId)).thenReturn(statement);
        when(clientService.updateClient(clientId, frrDto)).thenReturn(client);
        when(calculatorService.getCredit(any(ScoringDataDto.class)))
                .thenThrow(new CreditRequestFailedException("Credit calculation failed"));

        // Act & Assert
        assertThrows(CreditProcessingException.class,
                () -> creditService.finishCreditRegistration(statementId, frrDto));

        verify(statementService).updateStatementStatus(
                statementId,
                ApplicationStatus.CC_DENIED,
                ChangeType.AUTOMATIC
        );
    }

    @Test
    void finishCreditRegistration_WhenStatementNotFound_ShouldThrowException() {
        // Arrange
        UUID statementId = UUID.randomUUID();
        FinishRegistrationRequestDto frrDto = new FinishRegistrationRequestDto();

        when(statementService.getStatement(statementId))
                .thenThrow(new EntityNotFoundException("Statement not found"));

        // Act & Assert
        assertThrows(CreditProcessingException.class,
                () -> creditService.finishCreditRegistration(statementId, frrDto));
    }

    @Test
    void finishCreditRegistration_WhenClientNotFound_ShouldThrowException() throws Exception {
        // Arrange
        UUID statementId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        Client client = createTestClient(clientId);
        Statement statement = createTestStatement(statementId, client);
        FinishRegistrationRequestDto frrDto = createTestFinishRegistrationRequest();

        when(statementService.getStatement(statementId)).thenReturn(statement);
        when(clientService.updateClient(clientId, frrDto))
                .thenThrow(new EntityNotFoundException("Client not found"));

        // Act & Assert
        assertThrows(CreditProcessingException.class,
                () -> creditService.finishCreditRegistration(statementId, frrDto));
    }

    @Test
    void testBuildScoringDataDto() throws Exception {
        // Arrange
        UUID clientId = UUID.randomUUID();
        Client client = createTestClient(clientId);
        client.setGender(Gender.MALE);
        client.setMaritalStatus(MaritalStatus.MARRIED);
        client.setDependentAmount(2);
        client.setAccountNumber("1234567890");

        FinishRegistrationRequestDto frrDto = createTestFinishRegistrationRequest();
        frrDto.setPassportIssueDate(LocalDate.of(2010, 5, 15));

        Statement statement = createTestStatement(UUID.randomUUID(), client);

        Method method = CreditServiceImpl.class.getDeclaredMethod(
                "buildScoringDataDto", Client.class, FinishRegistrationRequestDto.class, Statement.class);
        method.setAccessible(true);

        // Act
        ScoringDataDto result = (ScoringDataDto) method.invoke(creditService, client, frrDto, statement);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(100000), result.getAmount());
        assertEquals(12, result.getTerm());
        assertEquals("Ivan", result.getFirstName());
        assertEquals("Ivanov", result.getLastName());
        assertEquals("Ivanovich", result.getMiddleName());
        assertEquals(Gender.MALE, result.getGender()); // Сравниваем enum с enum
        assertEquals(LocalDate.of(1990, 1, 1), result.getBirthdate());
        assertEquals("1234", result.getPassportSeries());
        assertEquals("567890", result.getPassportNumber());
        assertEquals(LocalDate.of(2010, 5, 15), result.getPassportIssueDate());
        assertEquals("УФМС РФ", result.getPassportIssueBranch());
        assertEquals(MaritalStatus.MARRIED, result.getMaritalStatus()); // Сравниваем enum с enum
        assertEquals(2, result.getDependentAmount());
        assertEquals("123456789012", result.getEmployment().getEmployerINN());
        assertEquals(BigDecimal.valueOf(50000), result.getEmployment().getSalary());
        assertEquals("1234567890", result.getAccountNumber());
        assertTrue(result.getIsInsuranceEnabled());
        assertFalse(result.getIsSalaryClient());
    }

    @Test
    void finishCreditRegistration_ShouldSetCorrectCreditStatus() throws Exception {
        // Arrange
        UUID statementId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        Client client = createTestClient(clientId);
        Statement statement = createTestStatement(statementId, client);
        FinishRegistrationRequestDto frrDto = createTestFinishRegistrationRequest();

        CreditDto creditDto = new CreditDto();
        creditDto.setAmount(BigDecimal.valueOf(100000));
        creditDto.setTerm(12);

        Credit credit = new Credit();
        credit.setCreditId(UUID.randomUUID());

        when(statementService.getStatement(statementId)).thenReturn(statement);
        when(clientService.updateClient(clientId, frrDto)).thenReturn(client);
        when(calculatorService.getCredit(any(ScoringDataDto.class))).thenReturn(creditDto);
        when(creditMapper.toEntity(creditDto)).thenReturn(credit);
        when(creditRepository.save(credit)).thenReturn(credit);

        // Act
        creditService.finishCreditRegistration(statementId, frrDto);

        // Assert
        assertEquals(CreditStatus.CALCULATED, credit.getCreditStatus());
    }
}

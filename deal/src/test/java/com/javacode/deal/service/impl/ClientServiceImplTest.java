package com.javacode.deal.service.impl;

import com.javacode.deal.dto.EmploymentDto;
import com.javacode.deal.dto.FinishRegistrationRequestDto;
import com.javacode.deal.dto.LoanStatementRequestDto;
import com.javacode.deal.model.entity.Client;
import com.javacode.deal.model.enums.EmploymentPosition;
import com.javacode.deal.model.enums.EmploymentStatus;
import com.javacode.deal.model.enums.Gender;
import com.javacode.deal.model.enums.MaritalStatus;
import com.javacode.deal.model.jsonb.Employment;
import com.javacode.deal.model.jsonb.Passport;
import com.javacode.deal.repository.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientServiceImpl clientService;

    @Test
    void createClient_ShouldCreateNewClient() {
        // Arrange
        LoanStatementRequestDto request = new LoanStatementRequestDto();
        request.setFirstName("Ivan");
        request.setLastName("Ivanov");
        request.setMiddleName("Ivanovich");
        request.setBirthdate(LocalDate.of(1990, 1, 1));
        request.setEmail("ivanov@example.com");
        request.setPassportSeries("1234");
        request.setPassportNumber("567890");

        Client expectedClient = Client.builder()
                .clientId(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Ivanov")
                .middleName("Ivanovich")
                .birthDate(LocalDate.of(1990, 1, 1))
                .email("ivanov@example.com")
                .passport(Passport.builder()
                        .series("1234")
                        .number("567890")
                        .build())
                .build();

        when(clientRepository.save(any(Client.class))).thenReturn(expectedClient);

        // Act
        Client result = clientService.createClient(request);

        // Assert
        assertNotNull(result);
        assertEquals("Ivan", result.getFirstName());
        assertEquals("Ivanov", result.getLastName());
        assertEquals("1234", result.getPassport().getSeries());
        assertEquals("567890", result.getPassport().getNumber());
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void updateClient_ShouldUpdateExistingClient() {
        // Arrange
        UUID clientId = UUID.randomUUID();
        FinishRegistrationRequestDto request = new FinishRegistrationRequestDto();
        request.setGender(Gender.MALE);
        request.setMaritalStatus(MaritalStatus.MARRIED);
        request.setDependentAmount(2);
        request.setPassportIssueDate(LocalDate.of(2010, 5, 15));
        request.setPassportIssueBranch("UFMS-1");
        request.setAccountNumber("1234567890");

        EmploymentDto employmentDto = new EmploymentDto();
        employmentDto.setEmploymentStatus(EmploymentStatus.EMPLOYED);
        employmentDto.setEmployerINN("123456789012");
        employmentDto.setSalary(BigDecimal.valueOf(50000.0));
        employmentDto.setEmploymentPosition(EmploymentPosition.MID_MANAGER);
        employmentDto.setWorkExperienceTotal(5);
        employmentDto.setWorkExperienceCurrent(2);
        request.setEmployment(employmentDto);

        Passport existingPassport = Passport.builder()
                .series("1234")
                .number("567890")
                .build();

        Client existingClient = Client.builder()
                .clientId(clientId)
                .firstName("Ivan")
                .lastName("Ivanov")
                .passport(existingPassport)
                .build();

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Client result = clientService.updateClient(clientId, request);

        // Assert
        assertNotNull(result);
        assertEquals(Gender.MALE, result.getGender());
        assertEquals(MaritalStatus.MARRIED, result.getMaritalStatus());
        assertEquals(2, result.getDependentAmount());
        assertEquals("UFMS-1", result.getPassport().getIssueBranch());
        assertEquals("1234567890", result.getAccountNumber());

        Employment updatedEmployment = result.getEmployment();
        assertNotNull(updatedEmployment);
        assertEquals(EmploymentStatus.EMPLOYED, updatedEmployment.getStatus());
        assertEquals("123456789012", updatedEmployment.getEmployerInn());
        assertEquals(BigDecimal.valueOf(50000.0), updatedEmployment.getSalary());

        verify(clientRepository, times(1)).findById(clientId);
        verify(clientRepository, times(1)).save(existingClient);
    }

    @Test
    void updateClient_WhenClientNotFound_ShouldThrowException() {
        // Arrange
        UUID nonExistentClientId = UUID.randomUUID();
        FinishRegistrationRequestDto request = new FinishRegistrationRequestDto();

        when(clientRepository.findById(nonExistentClientId)).thenReturn(Optional.empty());

        // Act
        assertThrows(EntityNotFoundException.class,
                () -> clientService.updateClient(nonExistentClientId, request));

        // Assert
        verify(clientRepository, times(1)).findById(nonExistentClientId);
        verify(clientRepository, never()).save(any());
    }
}

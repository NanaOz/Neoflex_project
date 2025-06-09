package com.javacode.microservice_calculate.service.impl;

import com.javacode.microservice_calculate.dto.EmploymentDto;
import com.javacode.microservice_calculate.dto.LoanOfferDto;
import com.javacode.microservice_calculate.dto.LoanStatementRequestDto;
import com.javacode.microservice_calculate.dto.ScoringDataDto;
import com.javacode.microservice_calculate.dto.enums.EmploymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorServiceTest {

    @InjectMocks
    private CalculatorServiceImpl calculatorService;

    @Mock
    private ScoringDataDto scoringDataDto;

    @Mock
    private LoanStatementRequestDto loanStatementRequestDto;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        calculatorService = new CalculatorServiceImpl();
        setPrivateField(calculatorService, "baseRate", new BigDecimal("10"));
        setPrivateField(calculatorService, "insuranceCost", new BigDecimal("1000"));
        setPrivateField(calculatorService, "insuranceRateReduction", new BigDecimal("1"));
        setPrivateField(calculatorService, "salaryClientRateReduction", new BigDecimal("0.5"));
    }

    private void setPrivateField(Object obj, String fieldName, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void calculateOffers_shouldGenerateFourOffers() {
        LoanStatementRequestDto request = new LoanStatementRequestDto();
        request.setAmount(new BigDecimal("100000"));
        request.setTerm(12);
        request.setBirthdate(LocalDate.of(1990, 1, 1));
        request.setEmail("test@example.com");
        request.setPassportSeries("1234");
        request.setPassportNumber("567890");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setMiddleName("Middle");

        List<LoanOfferDto> offers = calculatorService.calculateOffers(request);

        assertEquals(4, offers.size());
        assertTrue(offers.stream().anyMatch(o -> o.getIsInsuranceEnabled() && o.getIsSalaryClient()));
        assertTrue(offers.stream().anyMatch(o -> !o.getIsInsuranceEnabled() && !o.getIsSalaryClient()));
    }

    @Test
    void calculateFinalRate_ForUnemployedClient_ThrowsException() throws Exception {
        ScoringDataDto scoringData = new ScoringDataDto();
        EmploymentDto employment = new EmploymentDto();
        employment.setEmploymentStatus(EmploymentStatus.UNEMPLOYED);
        scoringData.setEmployment(employment);

        Method method = CalculatorServiceImpl.class.getDeclaredMethod("calculateFinalRate", ScoringDataDto.class);
        method.setAccessible(true);

        Exception exception = assertThrows(InvocationTargetException.class, () -> {
            method.invoke(calculatorService, scoringData);
        });

        assertEquals("Unemployed clients are not eligible for credit",
                exception.getCause().getMessage());
    }

    @Test
    public void calculateFinalRate_WithSelfEmployedClient_CorrectRate() throws Exception {
        ScoringDataDto scoringData = new ScoringDataDto();
        EmploymentDto employment = new EmploymentDto();
        employment.setEmploymentStatus(EmploymentStatus.SELF_EMPLOYED);
        scoringData.setEmployment(employment);
        scoringData.setBirthdate(LocalDate.of(1990, 1, 1));

        Method method = CalculatorServiceImpl.class.getDeclaredMethod("calculateFinalRate", ScoringDataDto.class);
        method.setAccessible(true);

        BigDecimal finalRate = (BigDecimal) method.invoke(calculatorService, scoringData);

        assertEquals(new BigDecimal("12"), finalRate);
    }

    @Test
    public void validateLoanAmount_BelowMinimum_ThrowsException() throws Exception {
        Method method = CalculatorServiceImpl.class.getDeclaredMethod("validateLoanAmount", BigDecimal.class);
        method.setAccessible(true);

        Exception exception = assertThrows(InvocationTargetException.class, () -> {
            method.invoke(calculatorService, new BigDecimal("15000"));
        });

        assertEquals("Сумма кредита должна составлять не менее 20000 долларов",
                exception.getCause().getMessage());
    }

    @Test
    public void validateLoanTerm_BelowMinimum_ThrowsException() throws Exception {
        Method method = CalculatorServiceImpl.class.getDeclaredMethod("validateLoanTerm", int.class);
        method.setAccessible(true);

        Exception exception = assertThrows(InvocationTargetException.class, () -> {
            method.invoke(calculatorService, 5);
        });

        assertEquals("Срок кредита должен составлять не менее 6 месяцев",
                exception.getCause().getMessage());
    }

    @Test
    public void validateBirthdate_AgeTooYoung_ThrowsException() throws Exception {
        Method method = CalculatorServiceImpl.class.getDeclaredMethod("validateBirthdate", LocalDate.class);
        method.setAccessible(true);

        Exception exception = assertThrows(InvocationTargetException.class, () -> {
            method.invoke(calculatorService, LocalDate.now());
        });

        assertEquals("Клиенту должно быть не менее 18лет",
                exception.getCause().getMessage());
    }

    @Test
    public void validateEmail_InvalidEmail_ThrowsException() throws Exception {
        Method method = CalculatorServiceImpl.class.getDeclaredMethod("validateEmail", String.class);
        method.setAccessible(true);

        Exception exception = assertThrows(InvocationTargetException.class, () -> {
            method.invoke(calculatorService, "invalid-email");
        });

        assertEquals("Неверный формат электронной почты",
                exception.getCause().getMessage());
    }

    @Test
    public void calculateMonthlyPayment() throws Exception {
        Method method = CalculatorServiceImpl.class.getDeclaredMethod("calculateMonthlyPayment",
                BigDecimal.class, BigDecimal.class, int.class);
        method.setAccessible(true);

        BigDecimal result = (BigDecimal) method.invoke(calculatorService,
                new BigDecimal("100000"), new BigDecimal("10"), 12);

        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    public void validateAge_AgeUnder20_ThrowsException() throws Exception {
        Method method = CalculatorServiceImpl.class.getDeclaredMethod("validateAge", LocalDate.class);
        method.setAccessible(true);

        Exception exception = assertThrows(InvocationTargetException.class, () -> {
            method.invoke(calculatorService, LocalDate.now().minusYears(19));
        });

        assertEquals("Возраст клиента должен быть от 20 до 65 лет", exception.getCause().getMessage());
    }

    @Test
    public void validateAge_AgeOver65_ThrowsException() throws Exception {
        Method method = CalculatorServiceImpl.class.getDeclaredMethod("validateAge", LocalDate.class);
        method.setAccessible(true);

        Exception exception = assertThrows(InvocationTargetException.class, () -> {
            method.invoke(calculatorService, LocalDate.now().minusYears(66));
        });

        assertEquals("Возраст клиента должен быть от 20 до 65 лет", exception.getCause().getMessage());
    }

    @Test
    public void validateName_InvalidCharacters_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            LoanStatementRequestDto request = new LoanStatementRequestDto();
            request.setFirstName("Иван");
            calculatorService.calculateOffers(request);
        });
    }

    @Test
    public void validateLoanAmountLimits_ExceedsMaxLoanAmount_ThrowsException() throws Exception {
        EmploymentDto employment = new EmploymentDto();
        employment.setSalary(new BigDecimal("1000"));

        ScoringDataDto scoringData = new ScoringDataDto();
        scoringData.setEmployment(employment);
        scoringData.setAmount(new BigDecimal("25000"));

        Method method = CalculatorServiceImpl.class.getDeclaredMethod("validateLoanAmountLimits", BigDecimal.class, BigDecimal.class);
        method.setAccessible(true);

        Exception exception = assertThrows(InvocationTargetException.class, () -> {
            method.invoke(calculatorService, scoringData.getAmount(), employment.getSalary());
        });

        assertEquals("Сумма кредита превышает 24 зарплаты", exception.getCause().getMessage());
    }
}

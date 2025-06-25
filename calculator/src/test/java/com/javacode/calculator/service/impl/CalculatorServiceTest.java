package com.javacode.calculator.service.impl;

import com.javacode.calculator.dto.Employment;
import com.javacode.calculator.dto.LoanOffer;
import com.javacode.calculator.dto.LoanStatementRequest;
import com.javacode.calculator.dto.ScoringData;
import com.javacode.calculator.handler.ScoringDataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculatorServiceTest {

    @InjectMocks
    private CalculatorServiceImpl calculatorService;

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);
        calculatorService = new CalculatorServiceImpl();
        setPrivateField(calculatorService, "baseRate", new BigDecimal("10"));
        setPrivateField(calculatorService, "insuranceCost", new BigDecimal("1000"));
        setPrivateField(calculatorService, "insuranceRateReduction", new BigDecimal("1"));
        setPrivateField(calculatorService, "salaryClientRateReduction", new BigDecimal("0.5"));
        setPrivateField(calculatorService, "selfEmployedRate", new BigDecimal("2"));
        setPrivateField(calculatorService, "businessOwnerRate", new BigDecimal("3"));
        setPrivateField(calculatorService, "midManagerRate", new BigDecimal("1"));
        setPrivateField(calculatorService, "topManagerRate", new BigDecimal("2"));
        setPrivateField(calculatorService, "marriedRate", new BigDecimal("1"));
        setPrivateField(calculatorService, "divorcedRate", new BigDecimal("1"));
        setPrivateField(calculatorService, "genderAgeRate", new BigDecimal("1"));
        setPrivateField(calculatorService, "nonBinaryRate", new BigDecimal("1"));
        setPrivateField(calculatorService, "femaleAgeMin", 20);
        setPrivateField(calculatorService, "femaleAgeMax", 65);
        setPrivateField(calculatorService, "maleAgeMin", 20);
        setPrivateField(calculatorService, "maleAgeMax", 65);
        setPrivateField(calculatorService, "minFinalRate", new BigDecimal("5"));
        setPrivateField(calculatorService, "percentDivisor", 100);
        setPrivateField(calculatorService, "monthsInYear", 12);
    }

    private void setPrivateField(Object obj, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    private LoanStatementRequest createValidLoanRequest() {
        LoanStatementRequest request = new LoanStatementRequest();
        request.setAmount(100000.0);
        request.setTerm(12);
        request.setBirthdate(LocalDate.of(1990, 1, 1));
        request.setEmail("test@example.com");
        request.setPassportSeries("1234");
        request.setPassportNumber("567890");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setMiddleName("Middle");
        return request;
    }

    private ScoringData createValidScoringData() {
        ScoringData scoringData = new ScoringData();
        scoringData.setAmount(100000.0);
        scoringData.setTerm(12);
        scoringData.setBirthdate(LocalDate.of(1990, 1, 1));
        scoringData.setPassportSeries("1234");
        scoringData.setPassportNumber("567890");
        scoringData.setFirstName("John");
        scoringData.setLastName("Doe");
        scoringData.setMiddleName("Middle");
        scoringData.setGender(ScoringData.GenderEnum.MALE);
        scoringData.setMaritalStatus(ScoringData.MaritalStatusEnum.SINGLE);
        scoringData.setDependentAmount(0);
        scoringData.setIsInsuranceEnabled(false);
        scoringData.setIsSalaryClient(false);

        Employment employment = new Employment();
        employment.setEmploymentStatus(Employment.EmploymentStatusEnum.EMPLOYED);
        employment.setEmployerINN("1234567890");
        employment.setSalary(50000.0);
        employment.setPosition(Employment.PositionEnum.MID_MANAGER);
        employment.setWorkExperienceTotal(60);
        employment.setWorkExperienceCurrent(24);

        scoringData.setEmployment(employment);
        return scoringData;
    }

    @Test
    void calculateOffers_shouldGenerateFourOffers() {
        LoanStatementRequest request = createValidLoanRequest();
        ResponseEntity<List<LoanOffer>> response = calculatorService.calculateOffers(request);
        assertNotNull(response.getBody());
        assertEquals(4, response.getBody().size());
    }

    @Test
    void calculateFinalRate_ForUnemployedClient_ThrowsException() throws Exception {
        ScoringData scoringData = createValidScoringData();
        scoringData.getEmployment().setEmploymentStatus(Employment.EmploymentStatusEnum.UNEMPLOYED);

        assertThrows(ScoringDataException.class, () -> {
            calculatorService.calculateCredit(scoringData);
        });
    }

    @Test
    public void calculateFinalRate_WithSelfEmployedClient_CorrectRate() throws Exception {
        ScoringData scoringData = new ScoringData();
        Employment employment = new Employment();
        employment.setEmploymentStatus(Employment.EmploymentStatusEnum.SELF_EMPLOYED);
        scoringData.setEmployment(employment);
        scoringData.setBirthdate(LocalDate.of(1990, 1, 1));

        Method method = CalculatorServiceImpl.class.getDeclaredMethod("calculateFinalRate", ScoringData.class);
        method.setAccessible(true);

        BigDecimal finalRate = (BigDecimal) method.invoke(calculatorService, scoringData);

        assertEquals(new BigDecimal("12"), finalRate);
    }

    @Test
    void createOffer_WithInsuranceAndSalaryClient_ReturnsCorrectOffer() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        LoanStatementRequest request = createValidLoanRequest();

        Method method = CalculatorServiceImpl.class.getDeclaredMethod(
                "createOffer",
                LoanStatementRequest.class,
                boolean.class,
                boolean.class
        );
        method.setAccessible(true);

        LoanOffer offer = (LoanOffer) method.invoke(calculatorService, request, true, true);

        assertEquals(101000.0, offer.getTotalAmount(), 0.001);
        assertEquals(8.5, offer.getRate(), 0.001);
    }

    @Test
    public void calculatePSK_ShouldReturnCorrectValue() throws Exception {
        BigDecimal amount = new BigDecimal("100000");
        BigDecimal monthlyPayment = new BigDecimal("8791.59");
        int term = 12;

        Method method = CalculatorServiceImpl.class.getDeclaredMethod(
                "calculatePSK",
                BigDecimal.class, BigDecimal.class, int.class
        );
        method.setAccessible(true);

        BigDecimal psk = (BigDecimal) method.invoke(
                calculatorService,
                amount, monthlyPayment, term
        );

        BigDecimal expected = monthlyPayment.multiply(new BigDecimal(term)).subtract(amount);
        assertEquals(expected, psk);
    }

    @Test
    void calculateMonthlyPayment_ReturnsCorrectValue() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method method = CalculatorServiceImpl.class.getDeclaredMethod(
                "calculateMonthlyPayment",
                BigDecimal.class, BigDecimal.class, int.class
        );
        method.setAccessible(true); // Делаем метод доступным


        BigDecimal payment = (BigDecimal) method.invoke(
                calculatorService,
                new BigDecimal("100000"),
                new BigDecimal("10"),
                12
        );

        assertTrue(payment.compareTo(new BigDecimal("8791.59")) == 0);
    }

    @Test
    void calculatePSK_ReturnsCorrectValue() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method method = CalculatorServiceImpl.class.getDeclaredMethod(
                "calculatePSK",
                BigDecimal.class, BigDecimal.class, int.class
        );
        method.setAccessible(true);

        BigDecimal psk = (BigDecimal) method.invoke(
                calculatorService,
                new BigDecimal("100000"),
                new BigDecimal("8791.59"),
                12
        );

        assertEquals(new BigDecimal("5499.08"), psk);
    }
}
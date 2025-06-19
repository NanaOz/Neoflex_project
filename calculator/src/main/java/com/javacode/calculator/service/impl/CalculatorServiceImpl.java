package com.javacode.calculator.service.impl;


import com.javacode.calculator.controller.CalculatorApi;
import com.javacode.calculator.dto.Credit;
import com.javacode.calculator.dto.Employment;
import com.javacode.calculator.dto.LoanOffer;
import com.javacode.calculator.dto.LoanStatementRequest;
import com.javacode.calculator.dto.PaymentScheduleElement;
import com.javacode.calculator.dto.ScoringData;
import com.javacode.calculator.handler.EmploymentValidationException;
import com.javacode.calculator.service.CalculatorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class CalculatorServiceImpl implements CalculatorService {
    @Value("${credit.base-rate}")
    private BigDecimal baseRate;
    @Value("${credit.insurance-cost}")
    private BigDecimal insuranceCost;
    @Value("${credit.insurance-rate-reduction}")
    private BigDecimal insuranceRateReduction;
    @Value("${credit.salary-client-rate-reduction}")
    private BigDecimal salaryClientRateReduction;
    @Value("${credit.min-final-rate}")
    private BigDecimal minFinalRate;
    @Value("${credit.rate.self-employed}")
    private BigDecimal selfEmployedRate;
    @Value("${credit.rate.business-owner}")
    private BigDecimal businessOwnerRate;
    @Value("${credit.rate.mid-manager}")
    private BigDecimal midManagerRate;
    @Value("${credit.rate.top-manager}")
    private BigDecimal topManagerRate;
    @Value("${credit.rate.married}")
    private BigDecimal marriedRate;
    @Value("${credit.rate.divorced}")
    private BigDecimal divorcedRate;
    @Value("${credit.rate.gender-age}")
    private BigDecimal genderAgeRate;
    @Value("${credit.rate.non-binary}")
    private BigDecimal nonBinaryRate;
    @Value("${credit.age.female.min}")
    private int femaleAgeMin;
    @Value("${credit.age.female.max}")
    private int femaleAgeMax;
    @Value("${credit.age.male.min}")
    private int maleAgeMin;
    @Value("${credit.age.male.max}")
    private int maleAgeMax;
    @Value("${calculation.months-in-year}")
    private int monthsInYear;
    @Value("${calculation.percent-divisor}")
    private int percentDivisor;
    @Value("${calculation.monthly-rate-divisor}")
    private int monthlyRateDivisor;

    @Override
    public ResponseEntity<List<LoanOffer>> calculateOffers(LoanStatementRequest requestDto) {
        List<LoanOffer> offers = new ArrayList<>();

        for (boolean insurance : new boolean[]{false, true}) {
            for (boolean salaryClient : new boolean[]{false, true}) {
                offers.add(createOffer(requestDto, insurance, salaryClient));
            }
        }

        offers.sort(Comparator.comparing(LoanOffer::getRate).reversed());
        return ResponseEntity.ok(offers);
    }

    @Override
    public ResponseEntity<Credit> calculateCredit(ScoringData scoringDataDto) {
        if (scoringDataDto.getEmployment().getEmploymentStatus() == Employment.EmploymentStatusEnum.UNEMPLOYED) {
            throw new EmploymentValidationException("Безработные клиенты не могут получить кредит");
        }

        BigDecimal rate = calculateFinalRate(scoringDataDto);
        BigDecimal totalAmount = BigDecimal.valueOf(scoringDataDto.getAmount());

        if (Boolean.TRUE.equals(scoringDataDto.getIsInsuranceEnabled())) {
            totalAmount = totalAmount.add(insuranceCost);
        }

        BigDecimal monthlyPayment = calculateMonthlyPayment(totalAmount, rate, scoringDataDto.getTerm());
        BigDecimal psk = calculatePSK(totalAmount, monthlyPayment, scoringDataDto.getTerm());
        List<PaymentScheduleElement> paymentSchedule = calculatePaymentSchedule(totalAmount, rate, scoringDataDto.getTerm());

        Credit credit = new Credit();
        credit.setAmount(totalAmount.doubleValue());
        credit.setTerm(scoringDataDto.getTerm());
        credit.setMonthlyPayment(monthlyPayment.doubleValue());
        credit.setRate(rate.doubleValue());
        credit.setPsk(psk.doubleValue());
        credit.setIsInsuranceEnabled(scoringDataDto.getIsInsuranceEnabled());
        credit.setIsSalaryClient(scoringDataDto.getIsSalaryClient());
        credit.setPaymentSchedule(paymentSchedule);

        return ResponseEntity.ok(credit);
    }

    /**
     * Создает кредитное предложение на основе входных данных и параметров страхования/зарплатного клиента
     *
     * @param requestDto         Данные запроса на кредитное предложение
     * @param isInsuranceEnabled Флаг подключения страховки
     * @param isSalaryClient     Флаг зарплатного клиента
     * @return Объект кредитного предложения с рассчитанными параметрами
     */
    private LoanOffer createOffer(LoanStatementRequest requestDto, boolean isInsuranceEnabled, boolean isSalaryClient) {
        BigDecimal rate = baseRate;
        BigDecimal totalAmount = BigDecimal.valueOf(requestDto.getAmount());

        if (isInsuranceEnabled) {
            rate = rate.subtract(insuranceRateReduction);
            totalAmount = totalAmount.add(insuranceCost);
        }

        if (isSalaryClient) {
            rate = rate.subtract(salaryClientRateReduction);
        }

        BigDecimal monthlyPayment = calculateMonthlyPayment(totalAmount, rate, requestDto.getTerm());

        LoanOffer offer = new LoanOffer();
        offer.setStatementId(UUID.randomUUID());
        offer.setRequestedAmount(requestDto.getAmount());
        offer.setTotalAmount(totalAmount.doubleValue());
        offer.setTerm(requestDto.getTerm());
        offer.setMonthlyPayment(monthlyPayment.doubleValue());
        offer.setRate(rate.doubleValue());
        offer.setIsInsuranceEnabled(isInsuranceEnabled);
        offer.setIsSalaryClient(isSalaryClient);

        return offer;
    }

    /**
     * Рассчитывает итоговую процентную ставку на основе данных скоринга
     *
     * @param scoringData Данные скоринга клиента
     * @return Итоговая процентная ставка с учетом всех корректировок
     * @throws IllegalArgumentException если клиент безработный
     */
    private BigDecimal calculateFinalRate(ScoringData scoringData) {
        BigDecimal rate = baseRate;

        // Корректировка ставки на основе данных скоринга
        switch (scoringData.getEmployment().getEmploymentStatus()) {
            case UNEMPLOYED:
                throw new EmploymentValidationException("Безработные клиенты не могут получить кредит");
            case SELF_EMPLOYED:
                rate = rate.add(selfEmployedRate);
                break;
            case BUSINESS_OWNER:
                rate = rate.add(businessOwnerRate);
                break;
        }

        if (scoringData.getEmployment().getPosition() == Employment.PositionEnum.MID_MANAGER) {
            rate = rate.subtract(midManagerRate);
        } else if (scoringData.getEmployment().getPosition() == Employment.PositionEnum.TOP_MANAGER) {
            rate = rate.subtract(topManagerRate);
        }

        if (scoringData.getMaritalStatus() == ScoringData.MaritalStatusEnum.MARRIED) {
            rate = rate.subtract(marriedRate);
        } else if (scoringData.getMaritalStatus() == ScoringData.MaritalStatusEnum.DIVORCED) {
            rate = rate.add(divorcedRate);
        }

        int age = Period.between(scoringData.getBirthdate(), LocalDate.now()).getYears();
        if (scoringData.getGender() == ScoringData.GenderEnum.FEMALE && age >= femaleAgeMin && age <= femaleAgeMax) {
            rate = rate.subtract(genderAgeRate);
        } else if (scoringData.getGender() == ScoringData.GenderEnum.MALE && age >= maleAgeMin && age <= maleAgeMax) {
            rate = rate.subtract(genderAgeRate);
        } else if (scoringData.getGender() == ScoringData.GenderEnum.NON_BINARY) {
            rate = rate.add(nonBinaryRate);
        }

        if (Boolean.TRUE.equals(scoringData.getIsInsuranceEnabled())) {
            rate = rate.subtract(insuranceRateReduction);
        }

        if (Boolean.TRUE.equals(scoringData.getIsSalaryClient())) {
            rate = rate.subtract(salaryClientRateReduction);
        }

        return rate.max(minFinalRate);
    }

    /**
     * Рассчитывает ежемесячный аннуитетный платеж по кредиту
     *
     * @param amount Сумма кредита
     * @param rate   Годовая процентная ставка
     * @param term   Срок кредита в месяцах
     * @return Размер ежемесячного платежа
     */
    private BigDecimal calculateMonthlyPayment(BigDecimal amount, BigDecimal rate, int term) {
        BigDecimal monthlyRate = rate.divide(new BigDecimal(percentDivisor * monthsInYear), 10, RoundingMode.HALF_UP);
        BigDecimal temp = BigDecimal.ONE.add(monthlyRate).pow(term);
        return amount.multiply(monthlyRate)
                .multiply(temp)
                .divide(temp.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
    }

    /**
     * Рассчитывает полную стоимость кредита (ПСК)
     *
     * @param amount         Сумма кредита
     * @param monthlyPayment Ежемесячный платеж
     * @param term           Срок кредита в месяцах
     * @return Полная стоимость кредита (переплата)
     */
    private BigDecimal calculatePSK(BigDecimal amount, BigDecimal monthlyPayment, int term) {
        return monthlyPayment.multiply(new BigDecimal(term)).subtract(amount);
    }

    /**
     * Генерирует график платежей по кредиту
     *
     * @param amount Сумма кредита
     * @param rate   Годовая процентная ставка
     * @param term   Срок кредита в месяцах
     * @return Список элементов графика платежей
     */
    private List<PaymentScheduleElement> calculatePaymentSchedule(BigDecimal amount, BigDecimal rate, int term) {
        List<PaymentScheduleElement> schedule = new ArrayList<>();
        BigDecimal monthlyRate = rate.divide(new BigDecimal(monthlyRateDivisor), 10, RoundingMode.HALF_UP);
        BigDecimal remainingDebt = amount;
        LocalDate paymentDate = LocalDate.now().plusMonths(1);

        for (int i = 1; i <= term; i++) {
            BigDecimal interestPayment = remainingDebt.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal monthlyPayment = calculateMonthlyPayment(amount, rate, term);
            BigDecimal debtPayment = monthlyPayment.subtract(interestPayment);

            remainingDebt = remainingDebt.subtract(debtPayment);
            if (remainingDebt.compareTo(BigDecimal.ZERO) < 0) {
                remainingDebt = BigDecimal.ZERO;
            }

            PaymentScheduleElement element = new PaymentScheduleElement();
            element.setNumber(i);
            element.setDate(paymentDate);
            element.setTotalPayment(monthlyPayment.doubleValue());
            element.setInterestPayment(interestPayment.doubleValue());
            element.setDebtPayment(debtPayment.doubleValue());
            element.setRemainingDebt(remainingDebt.doubleValue());

            schedule.add(element);
            paymentDate = paymentDate.plusMonths(1);
        }

        return schedule;
    }
}
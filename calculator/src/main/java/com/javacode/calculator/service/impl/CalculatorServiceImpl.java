package com.javacode.calculator.service.impl;

import com.javacode.calculator.dto.*;
import com.javacode.calculator.dto.enums.EmploymentStatus;
import com.javacode.calculator.dto.enums.Gender;
import com.javacode.calculator.dto.enums.MaritalStatus;
import com.javacode.calculator.dto.enums.Position;
import com.javacode.calculator.handler.ScoringDataException;
import com.javacode.calculator.service.CalculatorService;
import org.springframework.beans.factory.annotation.Value;
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

    @Override
    public List<LoanOfferDto> calculateOffers(LoanStatementRequestDto requestDto) {
        List<LoanOfferDto> offers = new ArrayList<>();

        for (boolean insurance : new boolean[]{false, true}) {
            for (boolean salaryClient : new boolean[]{false, true}) {
                offers.add(createOffer(requestDto, insurance, salaryClient));
            }
        }

        offers.sort(Comparator.comparing(LoanOfferDto::getRate).reversed());
        return offers;
    }

    @Override
    public CreditDto calculateCredit(ScoringDataDto scoringDataDto) {
        if (scoringDataDto.getEmployment().getEmploymentStatus() == EmploymentStatus.UNEMPLOYED) {
            throw new ScoringDataException("Безработные клиенты не могут получить кредит");
        }

        BigDecimal rate = calculateFinalRate(scoringDataDto);
        BigDecimal totalAmount = scoringDataDto.getAmount();

        if (Boolean.TRUE.equals(scoringDataDto.getIsInsuranceEnabled())) {
            totalAmount = totalAmount.add(insuranceCost);
        }

        BigDecimal monthlyPayment = calculateMonthlyPayment(totalAmount, rate, scoringDataDto.getTerm());
        BigDecimal psk = calculatePSK(totalAmount, monthlyPayment, scoringDataDto.getTerm());
        List<PaymentScheduleElementDto> paymentSchedule = calculatePaymentSchedule(totalAmount, rate, scoringDataDto.getTerm());

        CreditDto credit = new CreditDto();
        credit.setAmount(totalAmount);
        credit.setTerm(scoringDataDto.getTerm());
        credit.setMonthlyPayment(monthlyPayment);
        credit.setRate(rate);
        credit.setPsk(psk);
        credit.setIsInsuranceEnabled(scoringDataDto.getIsInsuranceEnabled());
        credit.setIsSalaryClient(scoringDataDto.getIsSalaryClient());
        credit.setPaymentSchedule(paymentSchedule);

        return credit;
    }

    /**
     * Создает кредитное предложение на основе входных данных и параметров страхования/зарплатного клиента
     *
     * @param requestDto         Данные запроса на кредитное предложение
     * @param isInsuranceEnabled Флаг подключения страховки
     * @param isSalaryClient     Флаг зарплатного клиента
     * @return Объект кредитного предложения с рассчитанными параметрами
     */
    private LoanOfferDto createOffer(LoanStatementRequestDto requestDto, boolean isInsuranceEnabled, boolean isSalaryClient) {
        BigDecimal rate = baseRate;
        BigDecimal totalAmount = requestDto.getAmount();

        if (isInsuranceEnabled) {
            rate = rate.subtract(insuranceRateReduction);
            totalAmount = totalAmount.add(insuranceCost);
        }

        if (isSalaryClient) {
            rate = rate.subtract(salaryClientRateReduction);
        }

        BigDecimal monthlyPayment = calculateMonthlyPayment(totalAmount, rate, requestDto.getTerm());

        LoanOfferDto offer = new LoanOfferDto();
        offer.setStatementId(UUID.randomUUID());
        offer.setRequestedAmount(requestDto.getAmount());
        offer.setTotalAmount(totalAmount);
        offer.setTerm(requestDto.getTerm());
        offer.setMonthlyPayment(monthlyPayment);
        offer.setRate(rate);
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
    private BigDecimal calculateFinalRate(ScoringDataDto scoringData) {
        BigDecimal rate = baseRate;

        // Корректировка ставки на основе данных скоринга
        switch (scoringData.getEmployment().getEmploymentStatus()) {
            case UNEMPLOYED:
                throw new ScoringDataException("Безработные клиенты не могут получить кредит");
            case SELF_EMPLOYED:
                rate = rate.add(selfEmployedRate);
                break;
            case BUSINESS_OWNER:
                rate = rate.add(businessOwnerRate);
                break;
        }

        if (scoringData.getEmployment().getPosition() == Position.MID_MANAGER) {
            rate = rate.subtract(midManagerRate);
        } else if (scoringData.getEmployment().getPosition() == Position.TOP_MANAGER) {
            rate = rate.subtract(topManagerRate);
        }

        if (scoringData.getMaritalStatus() == MaritalStatus.MARRIED) {
            rate = rate.subtract(marriedRate);
        } else if (scoringData.getMaritalStatus() == MaritalStatus.DIVORCED) {
            rate = rate.add(divorcedRate);
        }

        int age = Period.between(scoringData.getBirthdate(), LocalDate.now()).getYears();
        if (scoringData.getGender() == Gender.FEMALE && age >= femaleAgeMin && age <= femaleAgeMax) {
            rate = rate.subtract(genderAgeRate);
        } else if (scoringData.getGender() == Gender.MALE && age >= maleAgeMin && age <= maleAgeMax) {
            rate = rate.subtract(genderAgeRate);
        } else if (scoringData.getGender() == Gender.NON_BINARY) {
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
    private List<PaymentScheduleElementDto> calculatePaymentSchedule(BigDecimal amount, BigDecimal rate, int term) {
        List<PaymentScheduleElementDto> schedule = new ArrayList<>();
        BigDecimal monthlyRate = rate.divide(new BigDecimal("1200"), 10, RoundingMode.HALF_UP);
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

            PaymentScheduleElementDto element = new PaymentScheduleElementDto();
            element.setNumber(i);
            element.setDate(paymentDate);
            element.setTotalPayment(monthlyPayment);
            element.setInterestPayment(interestPayment);
            element.setDebtPayment(debtPayment);
            element.setRemainingDebt(remainingDebt);

            schedule.add(element);
            paymentDate = paymentDate.plusMonths(1);
        }

        return schedule;
    }
}
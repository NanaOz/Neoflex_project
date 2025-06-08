package com.javacode.microservice_calculate.service.impl;

import com.javacode.microservice_calculate.dto.*;
import com.javacode.microservice_calculate.dto.enums.Gender;
import com.javacode.microservice_calculate.dto.enums.MaritalStatus;
import com.javacode.microservice_calculate.dto.enums.Position;
import com.javacode.microservice_calculate.service.CalculatorService;
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

    private static final BigDecimal MIN_LOAN_AMOUNT = new BigDecimal("20000");
    private static final BigDecimal MIN_FINAL_RATE = new BigDecimal("5");
    private static final int MIN_LOAN_TERM = 6;
    private static final int MIN_AGE = 18;
    private static final int MIN_SCORING_AGE = 20;
    private static final int MAX_SCORING_AGE = 65;
    private static final int MIN_TOTAL_EXPERIENCE = 18;
    private static final int MIN_CURRENT_EXPERIENCE = 3;
    private static final int SALARY_MULTIPLIER = 24;
    private static final String EMAIL_REGEX = "^[a-z0-9A-Z_!#$%&'*+/=?`{|}~^.-]+@[a-z0-9A-Z.-]+$";
    private static final String PASSPORT_SERIES_REGEX = "\\d{4}";
    private static final String PASSPORT_NUMBER_REGEX = "\\d{6}";

    @Override
    public List<LoanOfferDto> calculateOffers(LoanStatementRequestDto requestDto) {
        validatePrescoringData(requestDto);

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
        validateScoringData(scoringDataDto);

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
                throw new IllegalArgumentException("Unemployed clients are not eligible for credit");
            case SELF_EMPLOYED:
                rate = rate.add(new BigDecimal("2"));
                break;
            case BUSINESS_OWNER:
                rate = rate.add(new BigDecimal("1"));
                break;
        }

        if (scoringData.getEmployment().getPosition() == Position.MID_MANAGER) {
            rate = rate.subtract(new BigDecimal("2"));
        } else if (scoringData.getEmployment().getPosition() == Position.TOP_MANAGER) {
            rate = rate.subtract(new BigDecimal("3"));
        }

        if (scoringData.getMaritalStatus() == MaritalStatus.MARRIED) {
            rate = rate.subtract(new BigDecimal("3"));
        } else if (scoringData.getMaritalStatus() == MaritalStatus.DIVORCED) {
            rate = rate.add(new BigDecimal("1"));
        }

        int age = Period.between(scoringData.getBirthdate(), LocalDate.now()).getYears();
        if (scoringData.getGender() == Gender.FEMALE && age >= 32 && age <= 60) {
            rate = rate.subtract(new BigDecimal("3"));
        } else if (scoringData.getGender() == Gender.MALE && age >= 30 && age <= 55) {
            rate = rate.subtract(new BigDecimal("3"));
        } else if (scoringData.getGender() == Gender.NON_BINARY) {
            rate = rate.add(new BigDecimal("7"));
        }

        if (Boolean.TRUE.equals(scoringData.getIsInsuranceEnabled())) {
            rate = rate.subtract(insuranceRateReduction);
        }

        if (Boolean.TRUE.equals(scoringData.getIsSalaryClient())) {
            rate = rate.subtract(salaryClientRateReduction);
        }

        return rate.max(MIN_FINAL_RATE);
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
        BigDecimal monthlyRate = rate.divide(new BigDecimal("1200"), 10, RoundingMode.HALF_UP);
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

    /**
     * Проверяет данные прескоринга на соответствие требованиям
     *
     * @param request Данные запроса на кредитное предложение
     */
    private void validatePrescoringData(LoanStatementRequestDto request) {
        validateNameFields(request.getFirstName(), request.getLastName(), request.getMiddleName());
        validateLoanAmount(request.getAmount());
        validateLoanTerm(request.getTerm());
        validateBirthdate(request.getBirthdate());
        validateEmail(request.getEmail());
        validatePassportData(request.getPassportSeries(), request.getPassportNumber());
    }

    /**
     * Проверяет данные скоринга на соответствие требованиям
     *
     * @param scoringData
     */
    private void validateScoringData(ScoringDataDto scoringData) {
        validateNameFields(scoringData.getFirstName(), scoringData.getLastName(), scoringData.getMiddleName());
        validateLoanAmount(scoringData.getAmount());
        validateLoanTerm(scoringData.getTerm());
        validateBirthdate(scoringData.getBirthdate());
        validatePassportData(scoringData.getPassportSeries(), scoringData.getPassportNumber());
        validateAge(scoringData.getBirthdate());
        validateWorkExperience(scoringData.getEmployment());
        validateLoanAmountLimits(scoringData.getAmount(), scoringData.getEmployment().getSalary());
    }

    /**
     * Проверяет корректность имени/фамилии/отчества
     *
     * @param name      Проверяемое имя
     * @param fieldName Название поля для сообщения об ошибке
     * @throws IllegalArgumentException если имя:
     *                                  - null
     *                                  - короче 2 или длиннее 30 символов
     *                                  - содержит нелатинские символы
     */
    private void validateName(String name, String fieldName) {
        if (name == null || name.length() < 2 || name.length() > 30) {
            throw new IllegalArgumentException(fieldName + " должно быть от 2 до 30 символов");
        }
        if (!name.matches("[a-zA-Z]+")) {
            throw new IllegalArgumentException(fieldName + " должен содержать только латинские буквы");
        }
    }

    /**
     * Проверяет поля имени на корректность.
     *
     * @throws IllegalArgumentException если имя или фамилия некорректны.
     */
    private void validateNameFields(String firstName, String lastName, String middleName) {
        validateName(firstName, "Имя");
        validateName(lastName, "Фамилия");
        if (middleName != null && !middleName.isEmpty()) {
            validateName(middleName, "Второе имя");
        }
    }

    /**
     * Проверяет сумму кредита на соответствие минимальному значению.
     *
     * @param amount Сумма кредита.
     * @throws IllegalArgumentException если сумма кредита меньше 20000.
     */
    private void validateLoanAmount(BigDecimal amount) {
        if (amount.compareTo(MIN_LOAN_AMOUNT) < 0) {
            throw new IllegalArgumentException("Сумма кредита должна составлять не менее " + MIN_LOAN_AMOUNT + " долларов");
        }
    }

    /**
     * Проверяет срок кредита на соответствие минимальному значению.
     *
     * @param term Срок кредита в месяцах.
     * @throws IllegalArgumentException если срок кредита меньше 6 месяцев.
     */
    private void validateLoanTerm(int term) {
        if (term < MIN_LOAN_TERM) {
            throw new IllegalArgumentException("Срок кредита должен составлять не менее " + MIN_LOAN_TERM + " месяцев");
        }
    }

    /**
     * Проверяет дату рождения клиента на соответствие возрастным требованиям.
     *
     * @param birthdate Дата рождения клиента.
     * @throws IllegalArgumentException если клиент младше 18 лет.
     */
    private void validateBirthdate(LocalDate birthdate) {
        LocalDate minBirthdate = LocalDate.now().minusYears(MIN_AGE);
        if (birthdate.isAfter(minBirthdate)) {
            throw new IllegalArgumentException("Клиенту должно быть не менее " + MIN_AGE + "лет");
        }
    }

    /**
     * Проверяет данные паспорта на соответствие формату.
     *
     * @param passportSeries Серия паспорта.
     * @param passportNumber Номер паспорта.
     * @throws IllegalArgumentException если серия или номер паспорта некорректны.
     */
    private void validatePassportData(String passportSeries, String passportNumber) {
        if (!passportSeries.matches(PASSPORT_SERIES_REGEX)) {
            throw new IllegalArgumentException("Серия паспорта должна состоять из 4 цифр");
        }
        if (!passportNumber.matches(PASSPORT_NUMBER_REGEX)) {
            throw new IllegalArgumentException("Номер паспорта должен состоять из 6 цифр");
        }
    }

    /**
     * Проверяет возраст клиента на соответствие диапазону.
     *
     * @param birthdate Дата рождения клиента.
     * @throws IllegalArgumentException если возраст клиента вне диапазона 20-65 лет.
     */
    private void validateAge(LocalDate birthdate) {
        int age = Period.between(birthdate, LocalDate.now()).getYears();
        if (age < MIN_SCORING_AGE || age > MAX_SCORING_AGE) {
            throw new IllegalArgumentException("Возраст клиента должен быть от " + MIN_SCORING_AGE + " до " + MAX_SCORING_AGE + " лет");
        }
    }

    /**
     * Проверяет опыт работы клиента на соответствие требованиям.
     *
     * @param employment Данные о трудоустройстве клиента.
     * @throws IllegalArgumentException если общий стаж менее 18 месяцев или текущий стаж менее 3 месяцев.
     */
    private void validateWorkExperience(EmploymentDto employment) {
        if (employment.getWorkExperienceTotal() < MIN_TOTAL_EXPERIENCE) {
            throw new IllegalArgumentException("Общий стаж работы должен составлять не менее " + MIN_TOTAL_EXPERIENCE + " месяцев");
        }

        if (employment.getWorkExperienceCurrent() < MIN_CURRENT_EXPERIENCE) {
            throw new IllegalArgumentException("Текущий стаж работы должен составлять не менее " + MIN_CURRENT_EXPERIENCE + " месяцев");
        }
    }

    /**
     * Проверяет сумму кредита на соответствие максимальному лимиту.
     *
     * @param amount Сумма кредита.
     * @param salary Зарплата клиента.
     * @throws IllegalArgumentException если сумма кредита превышает 24 зарплаты.
     */
    private void validateLoanAmountLimits(BigDecimal amount, BigDecimal salary) {
        BigDecimal maxLoanAmount = salary.multiply(new BigDecimal(SALARY_MULTIPLIER));
        if (amount.compareTo(maxLoanAmount) > 0) {
            throw new IllegalArgumentException("Сумма кредита превышает " + SALARY_MULTIPLIER + " зарплаты");
        }
    }

    /**
     * Проверяет корректность email на соответствие формату.
     *
     * @param email Электронная почта.
     * @throws IllegalArgumentException если формат email неверный.
     */
    private void validateEmail(String email) {
        if (!email.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Неверный формат электронной почты");
        }
    }
}
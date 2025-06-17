package com.javacode.calculator.dto;

import com.javacode.calculator.dto.enums.Gender;
import com.javacode.calculator.dto.enums.MaritalStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ScoringDataDto {
    @NotNull(message = "Сумма кредита не может быть пустой")
    @DecimalMin(value = "20000", message = "Минимальная сумма кредита - 20000")
    private BigDecimal amount;

    @NotNull(message = "Срок кредита не может быть пустым")
    @Min(value = 6, message = "Минимальный срок кредита - 6 месяцев")
    private Integer term;

    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 30, message = "Имя должно быть от 2 до 30 символов")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "Имя должно содержать только латинские буквы")
    private String firstName;

    @NotBlank(message = "Фамилия не может быть пустой")
    @Size(min = 2, max = 30, message = "Фамилия должна быть от 2 до 30 символов")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "Фамилия должна содержать только латинские буквы")
    private String lastName;

    @Size(min = 2, max = 30, message = "Отчество должно быть от 2 до 30 символов")
    @Pattern(regexp = "^[a-zA-Z]*$", message = "Отчество должно содержать только латинские буквы")
    private String middleName;

    @NotNull(message = "Пол не может быть пустым")
    private Gender gender;

    @NotNull(message = "Дата рождения не может быть пустой")
    @Past(message = "Дата рождения должна быть в прошлом")
    private LocalDate birthdate;

    @NotBlank(message = "Серия паспорта не может быть пустой")
    @Pattern(regexp = "\\d{4}", message = "Серия паспорта должна состоять из 4 цифр")
    private String passportSeries;

    @NotBlank(message = "Номер паспорта не может быть пустым")
    @Pattern(regexp = "\\d{6}", message = "Номер паспорта должен состоять из 6 цифр")
    private String passportNumber;

    @NotNull(message = "Дата выдачи паспорта не может быть пустой")
    private LocalDate passportIssueDate;

    @NotBlank(message = "Кем выдан паспорт не может быть пустым")
    private String passportIssueBranch;

    @NotNull(message = "Семейное положение не может быть пустым")
    private MaritalStatus maritalStatus;

    @Min(value = 0, message = "Количество иждивенцев не может быть отрицательным")
    private Integer dependentAmount;

    @Valid
    @NotNull(message = "Информация о занятости не может быть пустой")
    private EmploymentDto employment;

    private String accountNumber;
    private Boolean isInsuranceEnabled;
    private Boolean isSalaryClient;
}

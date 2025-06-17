package com.javacode.calculator.dto;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
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
public class LoanStatementRequestDto {
    @NotNull(message = "Сумма кредита обязательна")
    @DecimalMin(value = "20000", message = "Сумма кредита должна быть не менее {value}")
    private BigDecimal amount;

    @NotNull(message = "Срок кредита обязателен")
    @Min(value = 6, message = "Срок кредита должен быть не менее {value} месяцев")
    private Integer term;

    @NotBlank(message = "Имя обязательно для заполнения")
    @Size(min = 2, max = 30,
            message = "Имя должно быть от {min} до {max} символов")
    @Pattern(regexp = "[a-zA-Z]+", message = "Имя должно содержать только латинские буквы")
    private String firstName;

    @NotBlank(message = "Фамилия обязательна для заполнения")
    @Size(min = 2, max = 30,
            message = "Фамилия должна быть от {min} до {max} символов")
    @Pattern(regexp = "[a-zA-Z]+", message = "Фамилия должна содержать только латинские буквы")
    private String lastName;

    @Size(min = 2, max = 30,
            message = "Отчество должно быть от {min} до {max} символов")
    @Pattern(regexp = "[a-zA-Z]+", message = "Отчество должно содержать только латинские буквы")
    private String middleName;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    private String email;

    @NotNull(message = "Дата рождения не может быть пустой")
    @Past(message = "Дата рождения должна быть в прошлом")
    private LocalDate birthdate;

    @NotBlank(message = "Серия паспорта не может быть пустой")
    @Pattern(regexp = "\\d{4}", message = "Серия паспорта должна состоять из 4 цифр")
    private String passportSeries;

    @NotBlank(message = "Номер паспорта не может быть пустым")
    @Pattern(regexp = "\\d{6}", message = "Номер паспорта должен состоять из 6 цифр")
    private String passportNumber;
}

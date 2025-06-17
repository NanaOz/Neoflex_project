package com.javacode.microservice_calculate.dto;

import com.javacode.microservice_calculate.dto.enums.EmploymentStatus;
import com.javacode.microservice_calculate.dto.enums.Position;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmploymentDto {
    private EmploymentStatus employmentStatus;

    @NotBlank(message = "ИНН работодателя не может быть пустым")
    @Pattern(regexp = "\\d{10}|\\d{12}", message = "ИНН должен содержать 10 или 12 цифр")
    private String employerINN;

    @NotNull(message = "Зарплата не может быть пустой")
    @DecimalMin(value = "0", message = "Зарплата не может быть отрицательной")
    private BigDecimal salary;

    @NotNull(message = "Должность не может быть пустой")
    private Position position;

    @NotNull(message = "Общий стаж не может быть пустым")
    @Min(value = 12, message = "Общий стаж должен быть не менее 12 месяцев")
    private Integer workExperienceTotal;

    @NotNull(message = "Текущий стаж не может быть пустым")
    @Min(value = 3, message = "Текущий стаж должен быть не менее 3 месяцев")
    private Integer workExperienceCurrent;
}

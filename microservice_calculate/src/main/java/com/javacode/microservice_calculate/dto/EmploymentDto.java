package com.javacode.microservice_calculate.dto;

import com.javacode.microservice_calculate.dto.enums.EmploymentStatus;
import com.javacode.microservice_calculate.dto.enums.Position;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmploymentDto {
    private EmploymentStatus employmentStatus;
    private String employerINN;
    private BigDecimal salary;
    private Position position;
    private Integer workExperienceTotal;
    private Integer workExperienceCurrent;
}

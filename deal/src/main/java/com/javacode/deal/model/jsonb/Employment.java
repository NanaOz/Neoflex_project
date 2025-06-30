package com.javacode.deal.model.jsonb;

import com.javacode.deal.model.enums.EmploymentPosition;
import com.javacode.deal.model.enums.EmploymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "Employment")
public class Employment {
    private EmploymentStatus status;
    private String employerInn;
    private BigDecimal salary;
    private EmploymentPosition position;
    private Integer workExperienceTotal;
    private Integer workExperienceCurrent;
}

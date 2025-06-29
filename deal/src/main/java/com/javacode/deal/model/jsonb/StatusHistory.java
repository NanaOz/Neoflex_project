package com.javacode.deal.model.jsonb;

import com.javacode.deal.model.enums.ApplicationStatus;
import com.javacode.deal.model.enums.ChangeType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class StatusHistory {

    @NotNull(message = "Statement application status cannot be null")
    private ApplicationStatus status;

    @NotNull(message = "Timestamp cannot be null")
    private Timestamp time;

    @NotNull(message = "Statement change type cannot be null")
    private ChangeType changeType;
}

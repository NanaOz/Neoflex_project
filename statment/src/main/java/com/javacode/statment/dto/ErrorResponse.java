package com.javacode.statment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Error response")
public class ErrorResponse {
    @Schema(description = "Error code", example = "VALIDATION_ERROR")
    private String code;

    @Schema(description = "Error message", example = "Invalid input data")
    private String message;

    @Schema(description = "Error details")
    private List<String> details;

    public ErrorResponse() {
    }

    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ErrorResponse(String code, String message, List<String> details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }
}

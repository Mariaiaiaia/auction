package com.maria.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Error response")
public class ErrorResponse {
    @Schema(description = "Exception message", example = "Auction does not exist")
    private String errorMessage;

    public ErrorResponse(String error) {
        this.errorMessage = error;
    }

    public String getError() {
        return errorMessage;
    }
}

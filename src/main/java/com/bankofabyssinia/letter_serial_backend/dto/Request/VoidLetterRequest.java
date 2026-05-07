package com.bankofabyssinia.letter_serial_backend.dto.Request;

import jakarta.validation.constraints.NotBlank;

public class VoidLetterRequest {
    @NotBlank
    private String reason;

    public VoidLetterRequest() {}

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
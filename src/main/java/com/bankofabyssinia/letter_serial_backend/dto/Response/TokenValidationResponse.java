package com.bankofabyssinia.letter_serial_backend.dto.Response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenValidationResponse {
    @JsonProperty("isValid")
    private boolean valid;
    private String accessToken;
}

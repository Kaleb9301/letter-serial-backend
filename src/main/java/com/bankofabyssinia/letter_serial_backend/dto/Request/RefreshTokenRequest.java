package com.bankofabyssinia.letter_serial_backend.dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest implements BaseRequest {
    @NotBlank(message = "refreshToken is required")
    private String refreshToken;

    public RefreshTokenRequest() {
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}



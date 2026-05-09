package com.bankofabyssinia.letter_serial_backend.dto.Request;

import lombok.Data;

@Data
public class LogOutDto {
    private String accessToken;
    private String refreshToken;
}

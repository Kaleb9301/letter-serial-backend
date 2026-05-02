package com.bankofabyssinia.letter_serial_backend.dto.Response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LdapLoginResponse {

    private String accessToken;
    private String refreshToken;
    private String personNumber;
    private String fullName;
    private String permanentPosition;
    private String email;
    private String flag;
    private String actingPosition;
    private String department;
    private String jobTitle;
    private String orgId;
}

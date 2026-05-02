package com.bankofabyssinia.letter_serial_backend.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LdapLoginRequest implements BaseRequest {

    @NotBlank(message = "username is required")
    private String username;

    @NotBlank(message = "password is required")
    private String password;

    @NotNull(message = "applicationId is required")
    private Long applicationId;
}

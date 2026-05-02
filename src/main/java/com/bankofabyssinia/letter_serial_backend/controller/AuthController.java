package com.bankofabyssinia.letter_serial_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bankofabyssinia.letter_serial_backend.dto.Request.LdapLoginRequest;
import com.bankofabyssinia.letter_serial_backend.dto.Response.ApiResponse;
import com.bankofabyssinia.letter_serial_backend.dto.Response.LdapLoginResponse;
import com.bankofabyssinia.letter_serial_backend.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/auth")
public class AuthController extends BaseController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "LDAP login", description = "Delegates login to external auth-service LDAP endpoint")
    @PostMapping("/ldap-login")
    public ResponseEntity<ApiResponse<LdapLoginResponse>> ldapLogin(@Valid @RequestBody LdapLoginRequest request) {
        return ok("Login successful", authService.ldapLogin(request));
    }
}

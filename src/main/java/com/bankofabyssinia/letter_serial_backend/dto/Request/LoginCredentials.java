package com.bankofabyssinia.letter_serial_backend.dto.Request;

import jakarta.validation.constraints.NotBlank;

public class LoginCredentials {
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    private boolean forceLogin;

    public LoginCredentials() {}

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isForceLogin() {
        return forceLogin;
    }

    public void setForceLogin(boolean forceLogin) {
        this.forceLogin = forceLogin;
    }
}
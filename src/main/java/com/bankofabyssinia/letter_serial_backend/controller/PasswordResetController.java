package com.bankofabyssinia.letter_serial_backend.controller;

import com.bankofabyssinia.letter_serial_backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PasswordResetController {

    private final UserService userService;

    public PasswordResetController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/reset-password/request")
    public ResponseEntity<?> requestPasswordReset(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        String email = body.get("email");
        if (email == null || email.trim().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Email is required"));
        }

        String deviceFingerprint = body.getOrDefault("deviceFingerprint", "");

        // This call always returns true (anti-enumeration)
        userService.initiatePasswordReset(email.trim(), deviceFingerprint);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "If an account exists with this email, a reset code has been sent.");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password/complete")
    public ResponseEntity<?> completePasswordReset(@RequestBody Map<String, String> body) {

        String email = body.get("email");
        String otp = body.get("otp");
        String otpSessionId = body.get("otpSessionId");
        String newPassword = body.get("newPassword");

        if (email == null || otp == null || otpSessionId == null || newPassword == null ||
                email.trim().isBlank() || otp.trim().isBlank() || otpSessionId.trim().isBlank()
                || newPassword.trim().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Missing or invalid required fields"));
        }

        boolean success = userService.completePasswordReset(
                email.trim(),
                otp.trim(),
                otpSessionId.trim(),
                newPassword.trim());

        if (!success) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Invalid, expired, or incorrect reset information"));
        }

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Password has been successfully reset. Please log in."));
    }
}
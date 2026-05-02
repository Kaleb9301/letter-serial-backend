package com.bankofabyssinia.letter_serial_backend.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bankofabyssinia.letter_serial_backend.dto.Response.ApiResponse;

@RestController
@Profile("!prod")
@RequestMapping("/public")
public class TemplateTestController extends BaseController {

    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<Map<String, Object>>> ping() {
        Map<String, Object> payload = Map.of(
                "status", "UP",
                "service", "letter-serial-backend",
                "timestamp", LocalDateTime.now().toString());
        return ok("Template API is reachable", payload);
    }
}

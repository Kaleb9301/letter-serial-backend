package com.bankofabyssinia.letter_serial_backend.controller;

import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.bankofabyssinia.letter_serial_backend.dto.Response.ApiResponse;

public abstract class BaseController {

    protected <T> ResponseEntity<ApiResponse<T>> ok(String message, T data, String path) {
        return ResponseEntity.ok(new ApiResponse<>(200, true, OffsetDateTime.now().toString(), message, path, data));
    }

    protected ResponseEntity<ApiResponse<Void>> ok(String message, String path) {
        return ResponseEntity.ok(new ApiResponse<>(200, true, OffsetDateTime.now().toString(), message, path, null));
    }

    protected <T> ResponseEntity<ApiResponse<T>> created(String message, T data, String path) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, true, OffsetDateTime.now().toString(), message, path, data));
    }

    protected <T> ResponseEntity<ApiResponse<T>> fail(HttpStatus status, String message, String path) {
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(status.value(), false, OffsetDateTime.now().toString(), message, path, null));
    }
}

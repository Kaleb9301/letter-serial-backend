package com.bankofabyssinia.letter_serial_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.bankofabyssinia.letter_serial_backend.dto.Response.ApiResponse;

public abstract class BaseController {

    protected <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, data));
    }

    protected ResponseEntity<ApiResponse<Void>> ok(String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, null));
    }

    protected <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, message, data));
    }

    protected <T> ResponseEntity<ApiResponse<T>> fail(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(false, message, null));
    }
}

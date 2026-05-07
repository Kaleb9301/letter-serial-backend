package com.bankofabyssinia.letter_serial_backend.exception;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.bankofabyssinia.letter_serial_backend.dto.Response.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleJwtAuthentication(
        JwtAuthenticationException ex,
        HttpServletRequest request
    ) {
        return errorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, null);
    }
    
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleExternalService(
        ExternalServiceException ex,
        HttpServletRequest request
    ) {
        if (ex.getStatus() == HttpStatus.UNAUTHORIZED) {
            return errorResponse(HttpStatus.UNAUTHORIZED, "Invalid username or password", request, null);
        }

        return errorResponse(ex.getStatus(), ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    ) {
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::formatFieldError)
            .collect(Collectors.toList());

        return errorResponse(HttpStatus.BAD_REQUEST, "Validation failed", request, errors);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleBindException(
        BindException ex,
        HttpServletRequest request
    ) {
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::formatFieldError)
            .collect(Collectors.toList());

        return errorResponse(HttpStatus.BAD_REQUEST, "Binding failed", request, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleConstraintViolation(
        ConstraintViolationException ex,
        HttpServletRequest request
    ) {
        List<String> errors = ex.getConstraintViolations()
            .stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .collect(Collectors.toList());

        return errorResponse(HttpStatus.BAD_REQUEST, "Constraint violation", request, errors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleTypeMismatch(
        MethodArgumentTypeMismatchException ex,
        HttpServletRequest request
    ) {
        String expectedType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String detail = "Parameter '" + ex.getName() + "' should be of type " + expectedType;
        return errorResponse(HttpStatus.BAD_REQUEST, detail, request, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleMessageNotReadable(
        HttpMessageNotReadableException ex,
        HttpServletRequest request
    ) {
        return errorResponse(HttpStatus.BAD_REQUEST, "Malformed JSON request", request, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleMethodNotSupported(
        HttpRequestMethodNotSupportedException ex,
        HttpServletRequest request
    ) {
        return errorResponse(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), request, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleMediaTypeNotSupported(
        HttpMediaTypeNotSupportedException ex,
        HttpServletRequest request
    ) {
        return errorResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage(), request, null);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleMediaTypeNotAcceptable(
        HttpMediaTypeNotAcceptableException ex,
        HttpServletRequest request
    ) {
        return errorResponse(HttpStatus.NOT_ACCEPTABLE, ex.getMessage(), request, null);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleNoHandlerFound(
        NoHandlerFoundException ex,
        HttpServletRequest request
    ) {
        return errorResponse(HttpStatus.NOT_FOUND, "Resource not found", request, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleNoResourceFound(
        NoResourceFoundException ex,
        HttpServletRequest request
    ) {
        return errorResponse(HttpStatus.NOT_FOUND, "Resource not found", request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleAccessDenied(
        AccessDeniedException ex,
        HttpServletRequest request
    ) {
        return errorResponse(HttpStatus.FORBIDDEN, "Access denied", request, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleDataIntegrityViolation(
        DataIntegrityViolationException ex,
        HttpServletRequest request
    ) {
        return errorResponse(HttpStatus.CONFLICT, "Data integrity violation", request, null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleIllegalArgument(
        IllegalArgumentException ex,
        HttpServletRequest request
    ) {
        return errorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleUnhandledException(
        Exception ex,
        HttpServletRequest request
    ) {
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request, null);
    }

    private ResponseEntity<ApiResponse<Map<String, Object>>> errorResponse(
        HttpStatus status,
        String message,
        HttpServletRequest request,
        Object errors
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("timestamp", OffsetDateTime.now().toString());
        payload.put("status", status.value());
        payload.put("error", status.getReasonPhrase());
        payload.put("path", request.getRequestURI());
        if (errors != null) {
            payload.put("errors", errors);
        }

        return ResponseEntity
            .status(status)
            .body(new ApiResponse<>(false, message, payload));
    }

    private String formatFieldError(FieldError fieldError) {
        String defaultMessage = fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "is invalid";
        return fieldError.getField() + ": " + defaultMessage;
    }
}
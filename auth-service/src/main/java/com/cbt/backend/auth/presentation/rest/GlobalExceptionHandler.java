package com.cbt.backend.auth.presentation.rest;

import com.cbt.backend.auth.domain.exception.AccountDisabledException;
import com.cbt.backend.auth.domain.exception.InvalidCredentialsException;
import com.cbt.backend.auth.presentation.rest.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError("INVALID_CREDENTIALS", ex.getMessage()));
    }

    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<ApiError> handleAccountDisabled(AccountDisabledException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiError("ACCOUNT_DISABLED", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("VALIDATION_ERROR", "Request validation failed"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("MALFORMED_REQUEST", "Request body is missing or malformed"));
    }
}

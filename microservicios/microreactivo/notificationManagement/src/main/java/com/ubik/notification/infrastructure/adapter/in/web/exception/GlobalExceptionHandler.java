package com.ubik.notification.infrastructure.adapter.in.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para la API REST
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleRuntimeException(RuntimeException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }

    @ExceptionHandler(IllegalStateException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleIllegalStateException(IllegalStateException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ValidationErrorResponse>> handleValidationException(WebExchangeBindException ex) {
        Map<String, String> errors = ex.getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Error de validación",
                        (existing, replacement) -> existing
                ));

        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación",
                LocalDateTime.now(),
                errors
        );
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor: " + ex.getMessage(),
                LocalDateTime.now()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
    }

    /**
     * Respuesta de error estándar
     */
    public record ErrorResponse(
            int status,
            String message,
            LocalDateTime timestamp
    ) {
    }

    /**
     * Respuesta de error de validación
     */
    public record ValidationErrorResponse(
            int status,
            String message,
            LocalDateTime timestamp,
            Map<String, String> errors
    ) {
    }
}

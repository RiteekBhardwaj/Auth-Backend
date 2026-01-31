package com.riteek.store.exceptions;

import com.riteek.store.exceptions.CustomExceptions.*;
import com.riteek.store.exceptions.dtos.ExceptionResponse;
import com.riteek.store.exceptions.types.ErrorCodes;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //handle input validation exceptions
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        ExceptionResponse response = new ExceptionResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.name(),
                ErrorCodes.INVALID_INPUT,
                message,
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(response);
    }

    //handle business exceptions
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ExceptionResponse> handleBusinessExceptions(
            BusinessException ex,
            HttpServletRequest request) {

        ExceptionResponse response = new ExceptionResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.name(),
                ex.getErrorCode(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.internalServerError().body(response);
    }

    //handle service unavailable exceptions
    @ExceptionHandler(ServiceDownException.class)
    public ResponseEntity<ExceptionResponse> handleServiceUnavailableExceptions(
            ServiceDownException ex,
            HttpServletRequest request) {

        ExceptionResponse response = new ExceptionResponse(
                LocalDateTime.now(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                HttpStatus.SERVICE_UNAVAILABLE.name(),
                ex.getErrorCode(),
                ex.getClientMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.internalServerError().body(response);
    }

    //handle external service exceptions
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ExceptionResponse> handleExternalServiceExceptions(
            ExternalServiceException ex,
            HttpServletRequest request) {

        ExceptionResponse response = new ExceptionResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_GATEWAY.value(),
                HttpStatus.BAD_GATEWAY.name(),
                ex.getErrorCode(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.internalServerError().body(response);
    }

    //handle unknown exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleAllExceptions(
            Exception ex,
            HttpServletRequest request) {

        ExceptionResponse response = new ExceptionResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                ErrorCodes.UNKNOWN_ERROR,
                ErrorCodes.UNKNOWN_ERROR.getDefaultMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNotFoundExceptions(
            NotFoundException ex,
            HttpServletRequest request) {

        ExceptionResponse response = new ExceptionResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.name(),
                ErrorCodes.NOT_FOUND,
                ErrorCodes.NOT_FOUND.getDefaultMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

}

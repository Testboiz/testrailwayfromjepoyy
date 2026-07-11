package com.indivaragroup.jdt17wms.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ExceptionHandlingAdvice {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFoundException(NotFoundException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        errorResponse.put("code", HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequestException(BadRequestException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Invalid JSON Body");
        errorResponse.put("code", HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingRiskProfileException.class)
    public ResponseEntity<Map<String, Object>> handleMissingRiskProfileException(MissingRiskProfileException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        errorResponse.put("code", HttpStatus.UNPROCESSABLE_ENTITY.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(DelistedProductException.class)
    public ResponseEntity<Map<String, Object>> handleDelistedProductException(DelistedProductException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        errorResponse.put("type", "ERR-004");
        errorResponse.put("code", HttpStatus.UNPROCESSABLE_ENTITY.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(org.springframework.web.bind.MethodArgumentNotValidException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Invalid field values");
        errorResponse.put("type", "ERR-001");
        errorResponse.put("code", HttpStatus.BAD_REQUEST.value());

        java.util.List<Map<String, String>> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> {
                    Map<String, String> detail = new HashMap<>();
                    detail.put("field", fieldError.getField());
                    detail.put("reason", fieldError.getDefaultMessage());
                    return detail;
                })
                .collect(java.util.stream.Collectors.toList());

        errorResponse.put("details", details);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}


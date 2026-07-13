package com.indivaragroup.jdt17wms.exceptions;

import com.indivaragroup.jdt17wms.dto.utils.BusinessErrorResponseDTO;
import com.indivaragroup.jdt17wms.dto.utils.ValidationErrorDetailDTO;
import com.indivaragroup.jdt17wms.dto.utils.ValidationErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class ExceptionHandlingAdvice {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<BusinessErrorResponseDTO> handleNotFoundException(NotFoundException ex) {
        BusinessErrorResponseDTO errorResponse = BusinessErrorResponseDTO.builder()
                .error(ex.getMessage())
                .code(HttpStatus.NOT_FOUND.value())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<BusinessErrorResponseDTO> handleBadRequestException(BadRequestException ex) {
        BusinessErrorResponseDTO errorResponse = BusinessErrorResponseDTO.builder()
                .error("Invalid JSON Body")
                .code(HttpStatus.BAD_REQUEST.value())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingRiskProfileException.class)
    public ResponseEntity<BusinessErrorResponseDTO> handleMissingRiskProfileException(MissingRiskProfileException ex) {
        BusinessErrorResponseDTO errorResponse = BusinessErrorResponseDTO.builder()
                .error(ex.getMessage())
                .code(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(DelistedProductException.class)
    public ResponseEntity<BusinessErrorResponseDTO> handleDelistedProductException(DelistedProductException ex) {
        BusinessErrorResponseDTO errorResponse = BusinessErrorResponseDTO.builder()
                .error(ex.getMessage())
                .type("ERR-004")
                .code(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(DuplicatePriorityGoalException.class)
    public ResponseEntity<BusinessErrorResponseDTO> handleDuplicatePriorityGoalException(DuplicatePriorityGoalException ex) {
        BusinessErrorResponseDTO errorResponse = BusinessErrorResponseDTO.builder()
                .error(ex.getMessage())
                .type("ERR-002")
                .code(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(InsufficientIncomeException.class)
    public ResponseEntity<BusinessErrorResponseDTO> handleInsufficientIncomeException(InsufficientIncomeException ex) {
        BusinessErrorResponseDTO errorResponse = BusinessErrorResponseDTO.builder()
                .error(ex.getMessage())
                .type("ERR-003")
                .code(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ValidationErrorResponseDTO> handleValidationException(Exception ex) {
      BindingResult bindingResult = (ex instanceof MethodArgumentNotValidException methodArgumentNotValidException)
        ? methodArgumentNotValidException.getBindingResult()
        : (BindingResult) ex;

        List<ValidationErrorDetailDTO> details = bindingResult.getFieldErrors().stream()
                .map(fieldError -> {
                    String fieldName = fieldError.getField();
                    String snakeCaseField = fieldName.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
                    return ValidationErrorDetailDTO.builder()
                            .field(snakeCaseField)
                            .reason(fieldError.getDefaultMessage())
                            .build();
                })
                .toList();

        ValidationErrorResponseDTO errorResponse = ValidationErrorResponseDTO.builder()
                .error("Invalid field values")
                .type("ERR-001")
                .code(HttpStatus.BAD_REQUEST.value())
                .details(details)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}

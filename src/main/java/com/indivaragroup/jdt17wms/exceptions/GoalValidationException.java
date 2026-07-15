package com.indivaragroup.jdt17wms.exceptions;

import com.indivaragroup.jdt17wms.dto.utils.ValidationErrorDetailDTO;
import lombok.Getter;

import java.io.Serial;
import java.util.List;

/**
 * Domain-specific exception for goal field-level validation failures.
 * Carries a list of {@link ValidationErrorDetailDTO} describing each invalid field,
 * avoiding any coupling to Spring's {@link org.springframework.validation.BindException}.
 */
@Getter
public class GoalValidationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final transient List<ValidationErrorDetailDTO> errors;

    public GoalValidationException(List<ValidationErrorDetailDTO> errors) {
        super("Invalid goal field values");
        this.errors = errors;
    }
}

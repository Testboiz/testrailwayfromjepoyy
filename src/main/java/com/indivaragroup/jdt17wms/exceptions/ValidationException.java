package com.indivaragroup.jdt17wms.exceptions;

import com.indivaragroup.jdt17wms.dto.utils.ValidationErrorDetailDTO;
import lombok.Getter;
import java.util.List;

@Getter
public class ValidationException extends RuntimeException {
    private final List<ValidationErrorDetailDTO> details;
    private final String type;

    public ValidationException(List<ValidationErrorDetailDTO> details,String type) {
        super("Invalid field values");
        this.details = details;
        this.type = type;
    }
}
